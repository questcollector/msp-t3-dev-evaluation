package com.github.questcollector.evaluation.service

import com.slack.api.Slack
import com.slack.api.methods.request.auth.AuthTestRequest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@ExperimentalCoroutinesApi
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SlackUserInfoServiceTests {
    private val slack = Slack.getInstance()
    private val token = System.getenv("SLACK_BOT_TOKEN")
    private val userId = System.getenv("SLACK_USER_ID")
    @BeforeAll
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
    fun `slack username 조회하기`() {
        val slackUserInfoService = SlackUserInfoService(slack)
        slackUserInfoService.slackToken = token

        runTest {
            val result = slackUserInfoService.getSlackUserNameWithSlackUserId(userId)
            assertThat(result).isNotNull
        }
    }

    @Test
    fun `이상한 slack userid로 username 조회하기`() {
        val slackUserInfoService = SlackUserInfoService(slack)
        slackUserInfoService.slackToken = token

        runTest {
            val result = slackUserInfoService.getSlackUserNameWithSlackUserId("<<SLACK_USER_ID>>")
            assertThat(result).isNull()
        }
    }
}