package com.samsung.sds.t3.dev.evaluation.service

import com.slack.api.Slack
import com.slack.api.methods.request.users.UsersInfoRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class SlackUserInfoService (
    private val slack : Slack
) {
    @Value("\${slack.bot.token:dummy-token}")
    lateinit var slackToken: String

    suspend fun getSlackUserNameWithSlackUserId(slackUserId: String): String? {
        val response = slack.methods(slackToken).usersInfo(
            UsersInfoRequest.builder().user(slackUserId).build()
        )

        return if (response.isOk) response.user.realName else null
    }
}