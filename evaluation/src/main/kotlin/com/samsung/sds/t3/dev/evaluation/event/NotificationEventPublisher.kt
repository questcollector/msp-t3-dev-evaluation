package com.samsung.sds.t3.dev.evaluation.event

import com.samsung.sds.t3.dev.evaluation.model.MessageDataDTO
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

    suspend fun publishNotificationSuccessEvent(messageDataDTO: MessageDataDTO) {
        val payload = """
            @${messageDataDTO.slackUserName}님 개발 실습참여도 과제를 성공적으로 수행하였습니다.
            아래는 과제 제출 시 입력할 UUID 문자열입니다.
            ==========================================
            ${messageDataDTO.messageId}
            ==========================================
            
        """.trimIndent()
        val message = MessageBuilder.withPayload(payload)
            .setHeader("routingkey", messageDataDTO.hostname).build()
        streamBridge.send("notificationSuccessEvent-out-0",
            message)
        log.info("Success event: message was sent to ${messageDataDTO.hostname}")
        if (log.isDebugEnabled) log.debug("message payload: \n$payload")
    }

    suspend fun publishNotificationFailedEvent(hostname: String) {
        val payload = """
            조건이 충족되지 않았습니다. 다음 내용을 확인하세요
            1. application.properties의 slack.user.id 속성에 슬랙 멤버 아이디를 입력하기
            2. CampaignEventChannelInterceptor의 preSend() 메소드 구현하기
            
        """.trimIndent()
        val message = MessageBuilder.withPayload(payload)
            .setHeader("routingkey", hostname).build()
        streamBridge.send("notificationFailedEvent-out-0",
            message)
        log.info("Failed event: message was sent to $hostname")
        if (log.isDebugEnabled) log.debug("message payload: \n$payload")
    }
}