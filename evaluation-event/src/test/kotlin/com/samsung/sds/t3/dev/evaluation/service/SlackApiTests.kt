package com.samsung.sds.t3.dev.evaluation.service

import com.slack.api.Slack
import com.slack.api.methods.request.auth.AuthTestRequest
import com.slack.api.methods.request.chat.ChatPostMessageRequest
import com.slack.api.methods.request.conversations.ConversationsListRequest
import com.slack.api.methods.request.users.UsersInfoRequest
import com.slack.api.model.ConversationType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SlackApiTests {
    private val slack = Slack.getInstance()
    private val token = System.getenv("SLACK_USER_TOKEN")
    private val userId = System.getenv("SLACK_USER_ID")

    @BeforeEach
    fun `토큰이 있는지 점검`() {
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
            "mpim:read",
            "users:read",
            "chat:write"
        )
    }
    @Test
    fun `유저 이름 검색 테스트`() {
        val result = slack.methodsAsync(token).usersInfo(
            UsersInfoRequest.builder().user(userId).build()
        ).whenComplete { t, u ->
            println("users.info response: $t")
        }

        assertThat(result.get().isOk).isTrue

    }

    @Test
    fun `메시지 보내기 테스트`() {
        val channel = slack.methodsAsync(token).conversationsList(
            ConversationsListRequest.builder()
                .types(mutableListOf(ConversationType.IM))
                .build()
        ).get().channels.first { it.user == userId }.id

        val result = slack.methodsAsync(token).chatPostMessage(
            ChatPostMessageRequest.builder()
                .channel(channel)
                .text("<@$userId> test message")
                .mrkdwn(true)
                .build()
        ).whenCompleteAsync { t, u ->
            println(t)
        }

        assertThat(result.get().isOk).isTrue
    }
}