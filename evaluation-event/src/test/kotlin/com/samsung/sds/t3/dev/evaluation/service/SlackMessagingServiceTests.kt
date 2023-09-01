package com.samsung.sds.t3.dev.evaluation.service

import com.samsung.sds.t3.dev.evaluation.repository.entity.MessageDataEntity
import com.slack.api.Slack
import com.slack.api.methods.request.auth.AuthTestRequest
import com.slack.api.methods.response.conversations.ConversationsOpenResponse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.jvm.isAccessible

@ExperimentalCoroutinesApi
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

        runTest {
            val getDirectChannelMethod = slackMessagingService::class.declaredMemberFunctions
                .find{ it.name == "getDirectChannel" }
            getDirectChannelMethod?.let {
                it.isAccessible = true
                val result = it.callSuspend(slackMessagingService, userId) as ConversationsOpenResponse
                assertThat(result.channel.user).isEqualTo(userId)
            }
        }
    }

    @Test
    fun `이상한 slack user로 direct channel 조회하기`() {
        val slackMessagingService = SlackMessagingService(slack)
        slackMessagingService.slackToken = token

        runTest {
            val getDirectChannelMethod = slackMessagingService::class.declaredMemberFunctions
                .find{ it.name == "getDirectChannel" }
            getDirectChannelMethod?.let {
                it.isAccessible = true
                val result = it.callSuspend(slackMessagingService, "<<SLACK_USER_ID>>") as ConversationsOpenResponse
                assertThat(result.error).isEqualTo("user_not_found")
            }
        }
    }

    @Test
    fun `direct channel로 메시지 보내기 테스트`() {
        val slackMessagingService = SlackMessagingService(slack)
        slackMessagingService.slackToken = token

        val message = MessageDataEntity(
            id = UUID.randomUUID(),
            slackUserId = userId
        )

        runTest {
            val response = slackMessagingService.postMessage(message)
            assertThat(response.isOk).isTrue

        }
    }

    @Test
    fun `이상한 slack user로 메시지 보내기`() {
        val slackMessagingService = SlackMessagingService(slack)
        slackMessagingService.slackToken = token

        val message = MessageDataEntity(
            slackUserId = "<<SLACK_USER_ID>>"
        )

        runTest {
            assertThrows<NoSuchElementException> {
                slackMessagingService.postMessage(message)
            }
        }
    }
}