package com.samsung.sds.t3.dev.evaluation.service

import com.samsung.sds.t3.dev.evaluation.model.EvaluationResultDTO
import com.samsung.sds.t3.dev.evaluation.model.SlackMemberVO
import com.samsung.sds.t3.dev.evaluation.repository.MessageDataRepository
import com.samsung.sds.t3.dev.evaluation.repository.entity.toMessageDataDTO
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Service
class EvaluationResultService (
    private val messageDataRepository: MessageDataRepository
) {
    private val log : Logger = LoggerFactory.getLogger(this.javaClass)

    suspend fun getEvaluationResultBySlackUserId(
        slackUserId: String,
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime): EvaluationResultDTO {
        val messages = messageDataRepository.findAllBySlackUserId(slackUserId)

        var result = true
        var reason = "OK"

        if (messages.count() == 0) {
            result = false
            reason = "메시지 없음"
        } else {
            val filteredMessages = messages.filter {
                (it.sentDateTime.isAfter(startDateTime) || it.sentDateTime.isEqual(startDateTime))
                        && it.sentDateTime.isBefore(endDateTime)
            }

            val passedMessages = filteredMessages.filterNotNull().filter { it.isPass }

            val instanceId = filteredMessages.mapNotNull {
                it.instanceId
            }.filterNotNull().toSet()

            val ipAddress = filteredMessages.mapNotNull {
                it.ipAddress
            }.filterNotNull().toSet()

            if (log.isDebugEnabled) {
                log.debug("passed Message Count: ${filteredMessages.count()}")
                log.debug("instanceId: $instanceId")
                log.debug("ipAddress: $ipAddress")
            }

            if (passedMessages.count() == 0) {
                result = false
                reason = "통과한 메시지 없음"
            } else if (instanceId.count() > 1 || ipAddress.count() > 1) {
                result = false
                reason = "복수의 VM에서 실행한 것으로 보임"
            } else if (isCheated(instanceId.first())) {
                result = false
                reason = "하나의 VM에서 여러 명이 실행한 것으로 보임"
            }
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
    suspend fun isCheated(instanceId : String) : Boolean {
        val messages = messageDataRepository.findAllByInstanceId(instanceId)

        val slackUserIds = messages
            .filter { it.isPass }
            .mapNotNull { it.slackUserId }
            .filterNotNull().toSet()

        return slackUserIds.count() > 1
    }

    @FlowPreview
    suspend fun readCsv(data: Flow<ByteArray>) : Flow<SlackMemberVO> {
        // line이 잘렸을 때 앞 부분을 임시로 저장하는 변수
        var remainingBytes : ByteArray? = null

        return data
            // ByteArray -> String(line)
            .flatMapConcat { bytes -> flow<String> {
                val currentThread = Thread.currentThread()
                if (log.isDebugEnabled) log.debug("ByteArray -> String(line) Thread name: ${currentThread.name}")
                val line = StringBuilder()

                // 줄이 잘렸을 경우 잘린 앞부분을 저장한 배열과 합함
                val combinedData = if (remainingBytes != null) {
                    remainingBytes!! + bytes
                } else {
                    bytes
                }

                val rawData = combinedData.toString(Charsets.UTF_8)
                // 라인 분리
                for (char in rawData) {
                    when (char) {
                        '\n' -> {
                            if (log.isDebugEnabled) log.debug("currentLine: ${line.toString()}")
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

                // 줄이 잘린 경우 현재 라인을 배열에 저장
                remainingBytes = if (line.isNotEmpty()) {
                    if (log.isDebugEnabled) log.debug("remainingBytes: $line")
                    line.toString().toByteArray(Charsets.UTF_8)
                } else {
                    null
                }
            }}.buffer().filter {
                // 헤더 부분 제외
               !(it.startsWith("username,"))
            }.map {

                val currentThread = Thread.currentThread()
                if (log.isDebugEnabled) log.debug("String -> SlackMemberVO Thread name: ${currentThread.name}")

                // String row -> SlackMemberVO
                val columns = it.split(',', ignoreCase = false)

                val (status, billingActive, userId, fullname, displayname) =
                    arrayOf(
                        columns[2],
                        columns[3],
                        columns[6],
                        columns[7],
                        columns[8]
                    )

                SlackMemberVO(
                    status.trim(),
                    billingActive.toInt(),
                    userId.trim(),
                    fullname.trim().removeSurrounding("\""),
                    displayname.trim().removeSurrounding("\"")
                )

            }.filter {
                // Member(수강생 계정)와 활성화된 계정만 필터링
                (it.status == "Member" && it.billingActive == 1)
            }.catch {
                log.info(it.toString())
            }
    }
    suspend fun getResults(
        slackMembers: Flow<SlackMemberVO>,
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime) : Flow<SlackMemberVO> {

        return slackMembers.map {
            val currentThread = Thread.currentThread()
            val result = this.getEvaluationResultBySlackUserId(it.userId, startDateTime, endDateTime)
            it.result = result.reason
            if (log.isDebugEnabled) {
                log.debug("getResults Thread name: ${currentThread.name}")
                log.debug("${it.userId}: ${it.result}")
            }
            it
        }
    }

    @OptIn(FlowPreview::class)
    suspend fun writeCsv(slackMembers: Flow<SlackMemberVO>) : Flow<ByteArray> {
        // header
        val now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        val header = flowOf("userid,fullname,displayname,result_${now}\n".toByteArray())

        val rows = slackMembers.map { "${it.userId},\"${it.fullname}\",\"${it.displayname}\",${it.result}\n".toByteArray() }

        return flowOf(header, rows)
            .flattenConcat()
            .onEach {
                val currentThread = Thread.currentThread()
                if (log.isDebugEnabled) {
                    log.debug("write line Thread name: ${currentThread.name}")
                    log.debug("write line: ${String(it, Charsets.UTF_8)}")
                }
            }

    }
}