package com.github.questcollector.evaluation.service

import com.github.questcollector.evaluation.model.SampleDTO
import com.github.questcollector.evaluation.repository.MessageDataRepository
import com.github.questcollector.evaluation.repository.entity.MessageDataEntity
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.messaging.Message
import org.springframework.messaging.MessageHeaders
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.*

@Service
@Transactional
class MessageDataCommandService (
    private val messageDataRepository: MessageDataRepository,
    private val slackUserInfoService: SlackUserInfoService
) {

    private val log : Logger = LoggerFactory.getLogger(this.javaClass)

    private val zoneOffset = ZoneOffset.ofHours(9)
    private val instanceIdRegex = Regex("i-[0-9a-z]{17}")
    private val ipAddressRegex = Regex("172\\.31\\.\\d{1,3}\\.\\d{1,3}")

    suspend fun createMessageDataEntity(messageDataDTO: Message<SampleDTO>): MessageDataEntity {

        log.info("createMessageDataEntity invoked")

        val headers: MessageHeaders = messageDataDTO.headers
        val payload: SampleDTO = messageDataDTO.payload
        val slackUserName: String? = headers["SlackUserId"]?.let {
            slackUserInfoService.getSlackUserNameWithSlackUserId(it as String)
        }

        val sentDateTime = when(val timestamp = headers.timestamp) {
            null -> LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
            else -> {
                val epochSecond = timestamp / 1_000
                val nano = timestamp % 1_000 * 1_000_000
                LocalDateTime.ofEpochSecond(
                    epochSecond,
                    nano.toInt(),
                    zoneOffset
                )
            }
        }

        val messageEntity = MessageDataEntity(
            id = UUID.randomUUID(),
            sentDateTime = sentDateTime,
            instanceId = headers["InstanceId"] as? String,
            ipAddress = headers["IpAddress"] as? String,
            slackUserId = headers["SlackUserId"] as? String,
            slackUserName = slackUserName,
            payload = payload.toString(),
            isPass = calculateIsPass(headers, slackUserName)
        )

        return messageDataRepository.save(messageEntity)
    }

    private fun calculateIsPass(headers: MessageHeaders,
                                slackUserName: String?): Boolean {

        log.info("calculateIsPass invoked")

        val instanceId: String? = headers["InstanceId"] as? String
        val ipAddress: String? = headers["IpAddress"] as? String

        instanceId ?: return false
        ipAddress ?: return false
        slackUserName ?: return false

        return instanceIdRegex.matches(instanceId) && ipAddressRegex.matches(ipAddress)
    }
}