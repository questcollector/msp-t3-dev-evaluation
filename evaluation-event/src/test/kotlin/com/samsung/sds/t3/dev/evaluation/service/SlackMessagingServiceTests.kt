package com.samsung.sds.t3.dev.evaluation.service

import com.samsung.sds.t3.dev.evaluation.repository.entity.MessageDataEntity
import com.slack.api.Slack
import com.slack.api.methods.request.auth.AuthTestRequest
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.NoSuchElementException

class SlackMessagingServiceTests {

    private val slack = Slack.getInstance()
    private val token = System.getenv("SLACK_BOT_TOKEN")
    private val userId = System.getenv("SLACK_USER_ID")
    @BeforeEach
    fun `슬랙 테스트 사전점검`() {
        assumeTrue(token != null)
        assumeTrue(userId != null)
        val authTest = slack.methods(token).authTest(
            AuthTestRequest.builder().build()
        )
        val scopes = authTest.httpResponseHeaders["x-oauth-scopes"]?.get(0)?.split(",")
        assertThat(scopes).contains(
            "channels:read",
            "groups:read",
            "im:read",
            "im:write",
            "incoming-webhook",
            "mpim:read",
            "users:read",
            "chat:write"
        )
    }

    @Test
    fun `direct channel 조회하기`() {
        val slackMessagingService = SlackMessagingService(slack)
        slackMessagingService.slackToken = token

        runBlocking {
            val directChannel = slackMessagingService.getDirectChannel(userId)

            println(directChannel)
            assertThat(directChannel.channel.user).isEqualTo(userId)
        }
    }

    @Test
    fun `이상한 slack user로 direct channel 조회하기`() {
        val slackMessagingService = SlackMessagingService(slack)
        slackMessagingService.slackToken = token

        runBlocking {
            
            val directChannel = slackMessagingService.getDirectChannel("<<SLACK_USER_ID>>")
            assertThat(directChannel.error).isEqualTo("user_not_found")
            
        }
    }

    @Test
    fun `direct channel로 메시지 보내기 테스트`() {
        val slackMessagingService = SlackMessagingService(slack)
        slackMessagingService.slackToken = token

        val message = MessageDataEntity(
            slackUserId = userId,
            uuid = UUID.randomUUID()
        )

        runBlocking {
            val response = slackMessagingService.postMessage(message)
            assertThat(response.isOk).isTrue

        }
    }

    @Test
    fun `이상한 slack user로 메시지 보내기`() {
        val slackMessagingService = SlackMessagingService(slack)
        slackMessagingService.slackToken = token

        val message = MessageDataEntity(
            slackUserId = "<<SLACK_USER_ID>>",
            uuid = UUID.randomUUID()
        )

        runBlocking {
            assertThrows<NoSuchElementException> {
                slackMessagingService.postMessage(message)
            }
        }
    }
}