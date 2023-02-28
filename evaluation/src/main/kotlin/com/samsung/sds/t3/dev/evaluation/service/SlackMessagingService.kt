package com.samsung.sds.t3.dev.evaluation.service

import com.samsung.sds.t3.dev.evaluation.model.MessageDataDTO
import com.slack.api.Slack
import com.slack.api.webhook.Payload
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service


@Service
class SlackMessagingService (
    private val slack : Slack
) {

    private val log : Logger = LoggerFactory.getLogger(this.javaClass)

    @Value("\${slack.webhook.url:https://dummy-url}")
    lateinit var webhookUrl: String
    @Value("\${spring.profiles.active:test}")
    lateinit var springProfile: String

    suspend fun postMessage(message: MessageDataDTO) {
        val payload = Payload.builder().text(buildMessageContent(message)).build()
        log.info(payload.text)
        if (springProfile == "prod") {
            val response = slack.send(webhookUrl, payload)
            log.info("slack response info: $response")
        }
    }

    /**
     *
     * Generate Slack Message payload with Message
     * <br></br>
     * Slack Message formatting mkdwn reference:
     * <br></br>
     * https://api.slack.com/reference/surfaces/formatting
     *
     * @param message Message&lt;CampaignDTO&gt; contains Headers (SlackUserId,IpAddress, Hostname), Payload(CampaignDTO)
     * @return Formatted String as Slack postMessage api palyload
     */
    private fun buildMessageContent(message: MessageDataDTO): String {
        return """
            <@${message.slackUserId}>님 개발 실습참여도 과제를 성공적으로 수행하였습니다.
            아래는 과제 제출 시 입력할 UUID 문자열입니다.
            ==========================================
            *${message.messageId}*
            ==========================================
            
        """.trimIndent()
    }
}