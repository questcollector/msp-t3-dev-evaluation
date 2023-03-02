package com.samsung.sds.t3.dev.evaluation.service

import com.samsung.sds.t3.dev.evaluation.repository.entity.MessageDataEntity
import com.slack.api.Slack
import com.slack.api.methods.request.chat.ChatPostMessageRequest
import com.slack.api.methods.request.conversations.ConversationsListRequest
import com.slack.api.methods.response.chat.ChatPostMessageResponse
import com.slack.api.methods.response.conversations.ConversationsListResponse
import com.slack.api.model.ConversationType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


@Service
class SlackMessagingService (
    private val slack : Slack
) {

    private val log : Logger = LoggerFactory.getLogger(this.javaClass)

    @Value("\${slack.user.token:dummy-token}")
    lateinit var slackToken: String

    suspend fun postMessage(message: MessageDataEntity) : ChatPostMessageResponse {
        val payload = buildMessageContent(message)

        val channels = getDirectChannels()
        val directChannel = channels.channels.first { it.user == message.slackUserId }

        val response = slack.methodsAsync(slackToken).chatPostMessage(
            ChatPostMessageRequest.builder()
                .text(payload)
                .channel(directChannel.id)
                .build()
        )
        return suspendCoroutine {
            response.whenComplete { t, u ->
                if (t.isOk) {
                    log.info("Success postMessage")
                    if (log.isDebugEnabled) log.debug("send Message response: \n$t")
                    it.resume(t)
                } else {
                    log.info("Error on postMessage: ${t.error}")
                    if (log.isDebugEnabled) log.debug("Error on postMessage: \n$t")
                    it.resumeWithException(u)
                }
            }
        }
    }

    @Cacheable(cacheNames = arrayOf("directChannels"))
    suspend fun getDirectChannels() : ConversationsListResponse {
        val response = slack.methodsAsync(slackToken).conversationsList(
            ConversationsListRequest.builder()
                .types(mutableListOf(ConversationType.IM))
                .build()
        )
        return suspendCoroutine {
            response.whenComplete { t, u ->
                if (t.isOk) {
                    it.resume(t)
                } else {
                    log.info("Error on getDirectChannels: ${t.error}")
                    if (log.isDebugEnabled) log.debug("$t")
                    it.resumeWithException(u)
                }
            }
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
    private fun buildMessageContent(message: MessageDataEntity): String {
        return """
            <@${message.slackUserId}>님 개발 실습참여도 과제를 성공적으로 수행하였습니다.
            아래는 과제 제출 시 입력할 UUID 문자열입니다.
            ==========================================
            *${message.uuid}*
            ==========================================
            
        """.trimIndent()
    }
}