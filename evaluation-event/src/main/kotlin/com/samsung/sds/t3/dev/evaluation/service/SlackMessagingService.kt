package com.samsung.sds.t3.dev.evaluation.service

import com.samsung.sds.t3.dev.evaluation.repository.entity.MessageDataEntity
import com.slack.api.Slack
import com.slack.api.methods.request.chat.ChatPostMessageRequest
import com.slack.api.methods.request.conversations.ConversationsOpenRequest
import com.slack.api.methods.response.chat.ChatPostMessageResponse
import com.slack.api.methods.response.conversations.ConversationsOpenResponse
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

        log.info("postMessage invoked")

        val payload = buildMessageContent(message)

        val directChannel = getDirectChannel(message.slackUserId!!)
        if (!directChannel.isOk) throw NoSuchElementException(message.slackUserId)

        val response = slack.methodsAsync(slackToken).chatPostMessage(
            ChatPostMessageRequest.builder()
                .text(payload)
                .channel(directChannel.channel.id)
                .build()
        )
        return suspendCoroutine {
            response.whenComplete { t, u ->
                if (t.isOk) {
                    log.info("Success postMessage")
                    it.resume(t)
                } else {
                    log.info("Error on postMessage: ${t.error}")
                    it.resumeWithException(u)
                }
            }
        }
    }

    @Cacheable(cacheNames = ["directChannels"], key = "#slackUserId")
    private suspend fun getDirectChannel(slackUserId : String) : ConversationsOpenResponse {

        log.info("getDirectChannel invoked")

        val response = slack.methodsAsync(slackToken).conversationsOpen(
            ConversationsOpenRequest.builder()
                .returnIm(true)
                .users(listOf(slackUserId))
                .build()
        )
        return suspendCoroutine {
            response.whenComplete { t, _ ->
                if (t.isOk) {
                    log.info("Direct channel created")
                    it.resume(t)
                } else {
                    log.info("Error on getDirectChannels: ${t.error}")
                    it.resume(t)
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
            Excellent <@${message.slackUserId}>, 
            You have successfully completed the development practice assignment.
            You can check the same UUID below in the console logs on IntelliJ.
            ==========================================
            ${message.id}
            ==========================================
            
        """.trimIndent()
    }
}