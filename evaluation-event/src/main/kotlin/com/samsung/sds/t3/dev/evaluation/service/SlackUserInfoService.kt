package com.samsung.sds.t3.dev.evaluation.service

import com.slack.api.Slack
import com.slack.api.methods.request.users.UsersInfoRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Service
class SlackUserInfoService (
    private val slack : Slack
) {

    private val log : Logger = LoggerFactory.getLogger(this.javaClass)

    @Value("\${slack.user.token:dummy-token}")
    lateinit var slackToken: String

    @Cacheable(cacheNames = ["slackUserName"], key = "#slackUserId")
    suspend fun getSlackUserNameWithSlackUserId(slackUserId: String): String? {
        val response = slack.methodsAsync(slackToken).usersInfo(
            UsersInfoRequest.builder()
                .user(slackUserId)
                .build()
        )

        return suspendCoroutine {
            response.whenComplete { t, _ ->
                if (t.isOk) {
                    log.info("Success getSlackUserNameWithSlackUserId")
                    if (log.isDebugEnabled) log.debug("$t")
                    it.resume(t.user.realName)
                } else {
                    log.info("Error on getSlackUserNameWithSlackUserId")
                    if (log.isDebugEnabled) log.debug("$t")
                    it.resume(null)
                }
            }
        }
    }
}