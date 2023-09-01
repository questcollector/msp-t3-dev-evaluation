package com.samsung.sds.t3.dev.evaluation.service

import com.samsung.sds.t3.dev.evaluation.model.CampaignDTO
import com.samsung.sds.t3.dev.evaluation.repository.MessageDataRepository
import com.samsung.sds.t3.dev.evaluation.repository.entity.MessageDataEntity
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

    suspend fun createMessageDataEntity(messageDataDTO: Message<CampaignDTO>): MessageDataEntity {

        log.info("createMessageDataEntity invoked")

        val headers: MessageHeaders = messageDataDTO.headers
        val payload: CampaignDTO = messageDataDTO.payload
        val slackUserName: String? = headers["SlackUserId"]?.let {
            slackUserInfoService.getSlackUserNameWithSlackUserId(it as String)
        }

        val sentDateTime: LocalDateTime? = headers.timestamp?.run {
            val epochSecond = this / 1_000
            val nano = this % 1_000 * 1_000_000
            LocalDateTime.ofEpochSecond(
                epochSecond,
                nano.toInt(),
                ZoneOffset.ofHours(9)
            )
        }

        val messageEntity = MessageDataEntity(
            id = UUID.randomUUID(),
            sentDateTime = sentDateTime?: LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
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
        val instanceIdRegex = Regex("i-[0-9a-z]{17}")
        val ipAddressRegex = Regex("172\\.31\\.\\d{1,3}\\.\\d{1,3}")

        instanceId ?: return false
        ipAddress ?: return false
        slackUserName ?: return false

        if (!instanceIdRegex.matches(instanceId)) return false
        if (!ipAddressRegex.matches(ipAddress)) return false
        
        return true
    }
}