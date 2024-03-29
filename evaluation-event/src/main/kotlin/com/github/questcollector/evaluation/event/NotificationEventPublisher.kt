package com.github.questcollector.evaluation.event

import com.github.questcollector.evaluation.repository.entity.MessageDataEntity
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Component

@Component
class NotificationEventPublisher(
    private val streamBridge: StreamBridge
) {
    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    suspend fun publishNotificationSuccessEvent(messageDataDTO: MessageDataEntity) {
        val payload = """
            Excellent @${messageDataDTO.slackUserName}, 
            You have successfully completed the development practice assignment
            You can check the same UUID below in the DM on Slack
            ==========================================
            ${messageDataDTO.id}
            ==========================================
            
        """.trimIndent()
        val message = MessageBuilder.withPayload(payload)
            .setHeader("routingkey", messageDataDTO.slackUserId).build()
        streamBridge.send("notificationSuccessEvent-out-0",
            message)
        log.info("Success event: message was sent to ${messageDataDTO.slackUserId}")
    }

    suspend fun publishNotificationFailedEvent(messageDataDTO: MessageDataEntity) {
        val payload = """
            This is reply to the message from EC2 instance which id is ${messageDataDTO.instanceId}.
            The requirement was not fulfilled. Please check the following information.
            1. Please check "slack.user.id" property in application.properties file.
            2. Please check implementation of "CampaignEventChannelInterceptor.preSend()" method.
            
        """.trimIndent()
        val message = MessageBuilder.withPayload(payload)
            .setHeader("routingkey", messageDataDTO.slackUserId).build()
        streamBridge.send("notificationFailedEvent-out-0",
            message)
        log.info("Failed event: message was sent to ${messageDataDTO.slackUserId}")
    }
}