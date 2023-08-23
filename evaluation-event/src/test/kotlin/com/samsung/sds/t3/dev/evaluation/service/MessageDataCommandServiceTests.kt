package com.samsung.sds.t3.dev.evaluation.service

import com.samsung.sds.t3.dev.evaluation.model.CampaignDTO
import com.samsung.sds.t3.dev.evaluation.repository.MessageDataRepository
import com.samsung.sds.t3.dev.evaluation.repository.entity.MessageDataEntity
import io.mockk.coEvery
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.*

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
            val savedMessageDataEntity = messageDataCommandService.saveMessageDataEntity(createdMessageDataEntity)
            assertThat(savedMessageDataEntity)
                .isEqualTo(entity)
        }
    }

    @Test
    fun `통과 여부 계산 테스트`() {
        val message = org.springframework.messaging.support.MessageBuilder
            .withPayload(CampaignDTO())
            .setHeader("InstanceId", "i-02ee325b303f77f4f")
            .setHeader("IpAddress", "172.31.19.216")
            .setHeader("SlackUserId", "id")
            .build()

        coEvery { slackUserInfoService.getSlackUserNameWithSlackUserId("id") } returns "test"

        val messageDataCommandService = MessageDataCommandService(
            messageDataRepository,
            slackUserInfoService
        )
        
        runTest {
            val result = messageDataCommandService.createMessageDataEntity(message)
            assertThat(result.isPass).isTrue
        }
    }
}