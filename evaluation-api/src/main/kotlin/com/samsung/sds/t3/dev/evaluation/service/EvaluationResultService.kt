package com.samsung.sds.t3.dev.evaluation.service

import com.samsung.sds.t3.dev.evaluation.model.EvaluationResultDTO
import com.samsung.sds.t3.dev.evaluation.model.SlackMemberVO
import com.samsung.sds.t3.dev.evaluation.repository.MessageDataRepository
import com.samsung.sds.t3.dev.evaluation.repository.entity.toMessageDataDTO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Service
class EvaluationResultService(
    private val messageDataRepository: MessageDataRepository
) {
    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    suspend fun getEvaluationResultBySlackUserId(
        slackUserId: String,
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime
    ): EvaluationResultDTO {

        log.info("getEvaluationResultBySlackUserId invoked")

        val messages = messageDataRepository.findAllBySlackUserId(slackUserId)

        if (messages.count() == 0) {
            return EvaluationResultDTO(
                result = false,
                reason = "메시지 없음",
                data = emptyList()
            )
        }

        val filteredMessages = messages.filter { it.sentDateTime in (startDateTime..< endDateTime) }
        val passedMessages = filteredMessages.filter { it.isPass }
        val instanceId = filteredMessages.mapNotNull { it.instanceId }.toSet()
        val ipAddress = filteredMessages.mapNotNull { it.ipAddress }.toSet()

        val (result, reason) = when {
            (passedMessages.count() == 0) ->
                false to "통과한 메시지 없음"

            (instanceId.count() > 1 || ipAddress.count() > 1) ->
                false to "복수의 VM에서 실행한 것으로 보임"

            (isCheated(instanceId.first())) ->
                false to "하나의 VM에서 여러 명이 실행한 것으로 보임"

            else -> true to "OK"
        }

        val messageDTOs = messages.map { it.toMessageDataDTO() }.toList()

        return EvaluationResultDTO(
            result,
            reason,
            messageDTOs
        )
    }

    /**
     * 하나의 인스턴스에서 여러 개의 슬랙 아이디로 메시지를 처리한 경우
     */
    suspend fun isCheated(instanceId: String): Boolean {
        val messages = messageDataRepository.findAllByInstanceId(instanceId)

        val slackUserIds = messages.flowOn(Dispatchers.IO)
            .filter { it.isPass }
            .mapNotNull { it.slackUserId }
            .filterNotNull().toSet()

        return slackUserIds.count() > 1
    }

    @FlowPreview
    fun readCsv(data: Flow<ByteArray>): Flow<SlackMemberVO> {
        log.info("readCsv invoked")

        // line이 잘렸을 때 앞 부분을 임시로 저장하는 변수
        val remainingBytesChannel = Channel<ByteArray>(Channel.CONFLATED)

        CoroutineScope(Dispatchers.IO).launch {
            // send initial value as empty ByteArray
            remainingBytesChannel.send(ByteArray(0))
        }

        return data.flowOn(Dispatchers.IO)
            // ByteArray -> String(line)
            .flatMapConcat { bytes ->
                produceStringLineFromBytes(remainingBytesChannel, bytes)
            }.buffer()
            // 헤더 부분 제외
            .filter { !(it.startsWith("username,")) }
            // String row를 SlackMemberVO로 변환
            .map(::convertRowToSlackMemberVO)
            // Member(수강생 계정)와 활성화된 계정만 필터링
            .filter { (it.status == "Member" && it.billingActive == 1) }
    }

    private fun produceStringLineFromBytes(
        remainingBytesChannel: Channel<ByteArray>,
        bytes: ByteArray
    ) = flow {

        // flow로 넘어온 ByteArray와 remainingBytes 합치기
        val remainingBytes = remainingBytesChannel.receive()
        val combinedData = remainingBytes + bytes
        val rawData = combinedData.toString(Charsets.UTF_8)

        // 라인 분리
        val line = StringBuilder()
        for (char in rawData) {
            when (char) {
                '\n' -> {
                    // 데이터 완결 시 바로 publish
                    emit(line.toString())
                    line.clear()
                }

                '\r' -> {
                    // nothing, ignore carriage return
                }

                else -> line.append(char)
            }
        }

        // 남은 라인을 채널에 저장
        remainingBytesChannel.send(line.toString().toByteArray(Charsets.UTF_8))
    }

    private fun convertRowToSlackMemberVO(row: String): SlackMemberVO {

        val columns = row.split(',', ignoreCase = false)

        // columns 구성
        // username | email | status | billing-active | has-2fa | has-sso | userid | fullname | displayname | expiration-timestamp
        val (status, billingActive, userId, fullname, displayname) =
            arrayOf(
                columns[2],
                columns[3],
                columns[6],
                columns[7],
                columns[8]
            )

        return SlackMemberVO(
            status.trim(),
            billingActive.toInt(),
            userId.trim(),
            fullname.trim().removeSurrounding("\""),
            displayname.trim().removeSurrounding("\"")
        )
    }

    fun getResults(
        slackMembers: Flow<SlackMemberVO>,
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime
    ): Flow<SlackMemberVO> {

        log.info("getResults invoked")

        return slackMembers.flowOn(Dispatchers.IO)
            .map {
                val result = this.getEvaluationResultBySlackUserId(it.userId, startDateTime, endDateTime)
                it.result = result.reason

                return@map it
            }
    }

    @FlowPreview
    fun writeCsv(slackMembers: Flow<SlackMemberVO>): Flow<ByteArray> {

        log.info("writeCsv invoked")

        // header
        val now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        val header = flowOf("userid,fullname,displayname,result_${now}\n".toByteArray())

        val rows = slackMembers.map {
                "${it.userId},\"${it.fullname}\",\"${it.displayname}\",${it.result}\n".toByteArray()
            }

        return flowOf(header, rows).flowOn(Dispatchers.IO)
            .flattenConcat()
    }
}