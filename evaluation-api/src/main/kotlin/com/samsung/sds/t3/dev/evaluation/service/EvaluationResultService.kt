package com.samsung.sds.t3.dev.evaluation.service

import com.samsung.sds.t3.dev.evaluation.model.EvaluationResultDTO
import com.samsung.sds.t3.dev.evaluation.repository.MessageDataRepository
import com.samsung.sds.t3.dev.evaluation.repository.entity.toMessageDataDTO
import kotlinx.coroutines.flow.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class EvaluationResultService (
    private val messageDataRepository: MessageDataRepository
) {
    private val log : Logger = LoggerFactory.getLogger(this.javaClass)

    suspend fun getEvaluationResultBySlackUserName(
        slackUserName: String,
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime): EvaluationResultDTO {
        val messages = messageDataRepository.findAllBySlackUserNameStartsWith(slackUserName)

        var result = true
        var reason: String? = "OK"

        if (messages.count() == 0) {
            result = false
            reason = "메시지 없음"
        } else {
            val passedMessages = messages.filter {
                (it.isPass // 통과한 메시지만 필터링
                        && (it.sentDateTime!!.isAfter(startDateTime) || it.sentDateTime.isEqual(startDateTime))
                        && it.sentDateTime.isBefore(endDateTime))
            }
            val hostname = passedMessages.map {
                it.hostname
            }.toSet()

            val ipAddress = passedMessages.map {
                it.ipAddress
            }.toSet()

            if (log.isDebugEnabled) {
                log.debug("passed Message Count: ${passedMessages.count()}")
                log.debug("hostname: $hostname")
                log.debug("ipAddress: $ipAddress")
            }

            if (hostname.isEmpty() || ipAddress.isEmpty()) {
                result = false
                reason = "통과한 메시지 없음"
            } else if (hostname.count() > 1 || ipAddress.count() > 1) {
                result = false
                reason = "다른 VM에서 실행한 것으로 보임"
            }
        }

        val messageDTOs = messages.map { it.toMessageDataDTO() }.toList()

        return EvaluationResultDTO(
            result,
            reason,
            messageDTOs
        )
    }
}