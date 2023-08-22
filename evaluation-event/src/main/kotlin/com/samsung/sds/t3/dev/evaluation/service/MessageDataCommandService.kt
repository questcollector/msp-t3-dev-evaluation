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

        val headers: MessageHeaders = messageDataDTO.headers
        val payload: CampaignDTO = messageDataDTO.payload
        val slackUserName: String? = headers["SlackUserId"]?.run {
            slackUserInfoService.getSlackUserNameWithSlackUserId(this as String)
        }

        val isPass: Boolean = calculateIsPass(headers, slackUserName)

        val sentDateTime: LocalDateTime? = headers.timestamp?.run {
            val epochSecond = this / 1_000
            val nano = this % 1_000 * 1_000_000
            LocalDateTime.ofEpochSecond(
                epochSecond,
                nano.toInt(),
                ZoneOffset.ofHours(9)
            )
        }

        if (log.isDebugEnabled) log.debug("converted")

        return MessageDataEntity(
            UUID.randomUUID(),
            sentDateTime?: LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
            headers["InstanceId"] as? String,
            headers["IpAddress"] as? String,
            headers["SlackUserId"] as? String,
            slackUserName,
            payload.toString(),
            isPass
        )
    }

    suspend fun saveMessageDataEntity(messageDataEntity: MessageDataEntity) =
        messageDataRepository.save(messageDataEntity)

    fun calculateIsPass(headers: MessageHeaders,
                                slackUserName: String?): Boolean {

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