package com.samsung.sds.t3.dev.evaluation.service

import com.slack.api.Slack
import com.slack.api.methods.request.auth.AuthTestRequest
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SlackUserInfoServiceTests {
    private val slack = Slack.getInstance()
    private val token = System.getenv("SLACK_USER_TOKEN")
    private val userId = System.getenv("SLACK_USER_ID")
    @BeforeEach
    fun `슬랙 테스트 사전점검`() {
        Assumptions.assumeTrue(token != null)
        Assumptions.assumeTrue(userId != null)
        val authTest = slack.methods(token).authTest(
            AuthTestRequest.builder().build()
        )
        Assumptions.assumeTrue(authTest.isOk)
    }

    @Test
    fun `slack username 조회하기`() {
        val slackUserInfoService = SlackUserInfoService(slack)
        slackUserInfoService.slackToken = token

        runBlocking {
            val result = slackUserInfoService.getSlackUserNameWithSlackUserId(userId)
            assertThat(result).isNotNull
        }
    }

    @Test
    fun `이상한 slack userid로 username 조회하기`() {
        val slackUserInfoService = SlackUserInfoService(slack)
        slackUserInfoService.slackToken = token

        runBlocking {
            val result = slackUserInfoService.getSlackUserNameWithSlackUserId("<<SLACK_USER_ID>>")
            assertThat(result).isNull()
        }
    }
}