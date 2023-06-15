package com.samsung.sds.t3.dev.evaluation.service

import com.samsung.sds.t3.dev.evaluation.model.EvaluationResultDTO
import com.samsung.sds.t3.dev.evaluation.model.SlackMemberVO
import com.samsung.sds.t3.dev.evaluation.repository.MessageDataRepository
import com.samsung.sds.t3.dev.evaluation.repository.entity.toMessageDataDTO
import kotlinx.coroutines.flow.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
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

    suspend fun readCsv(inputStream: InputStream) : Flow<SlackMemberVO> {
        val reader = inputStream.bufferedReader()
        val header = reader.readLine()
        return reader.lineSequence().asFlow()
            .filter { it.isNotBlank() }
            .map {
                val columns = it.split(',', ignoreCase = false)
                val (status, billingActive, userId, fullname, displayname) =
                    arrayOf(columns[2], columns[3], columns[6], columns[7], columns[8])
                SlackMemberVO(
                    status.trim(),
                    billingActive.toInt(),
                    userId.trim(),
                    fullname.trim().removeSurrounding("\""),
                    displayname.trim().removeSurrounding("\"")
                )
            }.filter {
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
            val result = getEvaluationResultBySlackUserId(it.userId, startDateTime, endDateTime)
            it.result = result.reason
            if (log.isDebugEnabled) {
                log.debug("${it.userId}: ${it.result}")
            }
            it
        }
    }

}
suspend fun OutputStream.writeCsv(slackMembers: Flow<SlackMemberVO>) {
    val now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
    val writer = bufferedWriter()
    writer.write("""userid, fullname, displayname, result_${now}""")
    writer.newLine()
    slackMembers.onCompletion {
        writer.flush()
    }.map {
        writer.write("${it.userId}, \"${it.fullname}\", \"${it.displayname}\", ${it.result}")
        writer.newLine()
    }.collect()
}