package com.samsung.sds.t3.dev.evaluation.service

import com.samsung.sds.t3.dev.evaluation.model.CampaignDTO
import com.samsung.sds.t3.dev.evaluation.repository.MessageDataRepository
import com.samsung.sds.t3.dev.evaluation.repository.entity.MessageDataEntity
import io.mockk.coEvery
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.messaging.MessageHeaders
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.*

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class MessageDataCommandServiceTests {

    private val messageDataRepository = mockk<MessageDataRepository>(relaxed = true)

    private val slackUserInfoService = mockk<SlackUserInfoService>()

    @Test
    fun `메시지 추가 테스트`() {
        val campaignDTO = CampaignDTO(
            1, "test", "test"
        )
        val message = org.springframework.messaging.support.MessageBuilder
            .withPayload(campaignDTO)
            .setHeader("InstanceId", "instanceId")
            .setHeader("IpAddress", "ipAddress")
            .setHeader("SlackUserId", "id")
            .build()

        var sentDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
        message.headers.timestamp?.run {
            val epochSecond = this / 1000
            val nano = this % 1000 * 1000000
            sentDateTime = LocalDateTime.ofEpochSecond(
                    epochSecond, nano.toInt(), ZoneOffset.ofHours(9)
                )
        }

        val entity = MessageDataEntity(
            id = UUID.randomUUID(),
            sentDateTime = sentDateTime,
            instanceId = "instanceId",
            ipAddress = "ipAddress",
            slackUserId = "id",
            slackUserName = "test",
            payload = campaignDTO.toString(),
            isPass = false
        )

        coEvery { slackUserInfoService.getSlackUserNameWithSlackUserId("id") } returns "test"
        coEvery { messageDataRepository.save(any()) } returns entity


        val messageDataCommandService = MessageDataCommandService(
            messageDataRepository,
            slackUserInfoService
        )

        runTest {
            val createdMessageDataEntity = messageDataCommandService.createMessageDataEntity(message)
            assertThat(createdMessageDataEntity)
                .isEqualTo(entity)
        }
    }

    @Test
    fun `isPass = True 테스트`() {
        val message = org.springframework.messaging.support.MessageBuilder
            .withPayload(CampaignDTO())
            .setHeader("InstanceId", "i-02ee325b303f77f4f")
            .setHeader("IpAddress", "172.31.19.216")
            .setHeader("SlackUserId", "id")
            .build()

        val messageDataCommandService = MessageDataCommandService(
            messageDataRepository,
            slackUserInfoService
        )

        val calculateIsPass = messageDataCommandService.javaClass.getDeclaredMethod("calculateIsPass",
            MessageHeaders::class.java, String::class.java
        )
        calculateIsPass.trySetAccessible()

        
        runTest {
            val result = calculateIsPass.invoke(messageDataCommandService, message.headers, "test") as Boolean
            assertThat(result).isTrue
        }
    }

    @Test
    fun `isPass = False, instanceId 없는 경우`() {
        val message = org.springframework.messaging.support.MessageBuilder
            .withPayload(CampaignDTO())
            .setHeader("InstanceId", null)
            .setHeader("IpAddress", "172.31.19.216")
            .setHeader("SlackUserId", "id")
            .build()

        val messageDataCommandService = MessageDataCommandService(
            messageDataRepository,
            slackUserInfoService
        )

        val calculateIsPass = messageDataCommandService.javaClass.getDeclaredMethod("calculateIsPass",
            MessageHeaders::class.java, String::class.java
        )
        calculateIsPass.trySetAccessible()


        runTest {
            val result = calculateIsPass.invoke(messageDataCommandService, message.headers, "test") as Boolean
            assertThat(result).isFalse
        }
    }

    @Test
    fun `isPass = False, ipAddress 없는 경우`() {
        val message = org.springframework.messaging.support.MessageBuilder
            .withPayload(CampaignDTO())
            .setHeader("InstanceId", "i-02ee325b303f77f4f")
            .setHeader("IpAddress", null)
            .setHeader("SlackUserId", "id")
            .build()

        val messageDataCommandService = MessageDataCommandService(
            messageDataRepository,
            slackUserInfoService
        )

        val calculateIsPass = messageDataCommandService.javaClass.getDeclaredMethod("calculateIsPass",
            MessageHeaders::class.java, String::class.java
        )
        calculateIsPass.trySetAccessible()


        runTest {
            val result = calculateIsPass.invoke(messageDataCommandService, message.headers, "test") as Boolean
            assertThat(result).isFalse
        }
    }

    @Test
    fun `isPass = False, slackUserName 없는 경우`() {
        val message = org.springframework.messaging.support.MessageBuilder
            .withPayload(CampaignDTO())
            .setHeader("InstanceId", "i-02ee325b303f77f4f")
            .setHeader("IpAddress", "172.31.19.216")
            .setHeader("SlackUserId", "id")
            .build()

        val messageDataCommandService = MessageDataCommandService(
            messageDataRepository,
            slackUserInfoService
        )

        val calculateIsPass = messageDataCommandService.javaClass.getDeclaredMethod("calculateIsPass",
            MessageHeaders::class.java, String::class.java
        )
        calculateIsPass.trySetAccessible()


        runTest {
            val result = calculateIsPass.invoke(messageDataCommandService, message.headers, null) as Boolean
            assertThat(result).isFalse
        }
    }

}