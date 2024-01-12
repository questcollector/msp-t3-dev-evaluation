package com.github.questcollector.evaluation.service

import com.github.questcollector.evaluation.model.SampleDTO
import com.github.questcollector.evaluation.repository.MessageDataRepository
import com.github.questcollector.evaluation.repository.entity.MessageDataEntity
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
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.jvm.isAccessible

private val zoneOffset = ZoneOffset.ofHours(9)

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class MessageDataCommandServiceTests {

    private val messageDataRepository = mockk<MessageDataRepository>(relaxed = true)

    private val slackUserInfoService = mockk<SlackUserInfoService>()

    @Test
    fun `메시지 추가 테스트`() {
        val campaignDTO = SampleDTO(
            1, "test", "test"
        )
        val message = org.springframework.messaging.support.MessageBuilder
            .withPayload(campaignDTO)
            .setHeader("InstanceId", "instanceId")
            .setHeader("IpAddress", "ipAddress")
            .setHeader("SlackUserId", "id")
            .build()

        val sentDateTime = when(val timestamp = message.headers.timestamp) {
            null -> LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
            else -> {
                val epochSecond = timestamp / 1_000
                val nano = timestamp % 1_000 * 1_000_000
                LocalDateTime.ofEpochSecond(
                    epochSecond, nano.toInt(), zoneOffset
                )
            }
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
            .withPayload(SampleDTO(1, "test"))
            .setHeader("InstanceId", "i-02ee325b303f77f4f")
            .setHeader("IpAddress", "172.31.19.216")
            .setHeader("SlackUserId", "id")
            .build()

        val messageDataCommandService = MessageDataCommandService(
            messageDataRepository,
            slackUserInfoService
        )

        val calculateIsPassMethod = messageDataCommandService::class.declaredMemberFunctions
            .find { it.name == "calculateIsPass" }
        val result = calculateIsPassMethod?.let {
            it.isAccessible = true
            it.call(messageDataCommandService, message.headers, "test") as Boolean
        }
        assertThat(result).isTrue
    }

    @Test
    fun `isPass = False, instanceId 없는 경우`() {
        val message = org.springframework.messaging.support.MessageBuilder
            .withPayload(SampleDTO(1, "test"))
            .setHeader("InstanceId", null)
            .setHeader("IpAddress", "172.31.19.216")
            .setHeader("SlackUserId", "id")
            .build()

        val messageDataCommandService = MessageDataCommandService(
            messageDataRepository,
            slackUserInfoService
        )

        val calculateIsPassMethod = messageDataCommandService::class.declaredMemberFunctions
            .find { it.name == "calculateIsPass" }
        val result = calculateIsPassMethod?.let {
            it.isAccessible = true
            it.call(messageDataCommandService, message.headers, "test") as Boolean
        }
        assertThat(result).isFalse
    }

    @Test
    fun `isPass = False, ipAddress 없는 경우`() {
        val message = org.springframework.messaging.support.MessageBuilder
            .withPayload(SampleDTO(1, "test"))
            .setHeader("InstanceId", "i-02ee325b303f77f4f")
            .setHeader("IpAddress", null)
            .setHeader("SlackUserId", "id")
            .build()

        val messageDataCommandService = MessageDataCommandService(
            messageDataRepository,
            slackUserInfoService
        )

        val calculateIsPassMethod = messageDataCommandService::class.declaredMemberFunctions
            .find { it.name == "calculateIsPass" }
        val result = calculateIsPassMethod?.let {
            it.isAccessible = true
            it.call(messageDataCommandService, message.headers, "test") as Boolean
        }
        assertThat(result).isFalse
    }

    @Test
    fun `isPass = False, slackUserName 없는 경우`() {
        val message = org.springframework.messaging.support.MessageBuilder
            .withPayload(SampleDTO(1, "test"))
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

        val result = calculateIsPass.invoke(messageDataCommandService, message.headers, null) as Boolean
        assertThat(result).isFalse
    }

}