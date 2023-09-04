package com.samsung.sds.t3.dev.evaluation.event

import com.samsung.sds.t3.dev.evaluation.model.CampaignDTO
import com.samsung.sds.t3.dev.evaluation.repository.entity.MessageDataEntity
import com.samsung.sds.t3.dev.evaluation.service.MessageDataCommandService
import com.samsung.sds.t3.dev.evaluation.service.SlackMessagingService
import io.mockk.coEvery
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.messaging.support.GenericMessage
import org.springframework.messaging.support.MessageBuilder
import reactor.core.publisher.Flux


@ExtendWith(MockKExtension::class)
class CampaignAddedEventTests {
    private val messageDataCommandService = mockk<MessageDataCommandService>()
    private val slackMessagingService = mockk<SlackMessagingService>()
    private val notificationEventPublisher = mockk<NotificationEventPublisher>()

    @Test
    fun `consumer test`() = runBlocking {

        val campaigns = Flux.just(
            CampaignDTO(campaignId = 1, campaignName = "name1"),
            CampaignDTO(campaignId = 2, campaignName = "name2"),
            CampaignDTO(campaignId = 3, campaignName = "name3")
        ).map {
            MessageBuilder.withPayload(it).build()
        }

        val campaignAddedEventListener = spyk(CampaignAddedEventListener(
            messageDataCommandService,
            slackMessagingService,
            notificationEventPublisher
        ))

        coEvery { messageDataCommandService.createMessageDataEntity(any()) } coAnswers {
            println("handleMessage invoked")
            val argumentMessage = this.arg<GenericMessage<CampaignDTO>>(0)
            assertThat(argumentMessage.payload)
                .matches { it.campaignId in 1 ..< 3 }
                .matches { it.campaignName in listOf("name1", "name2", "name3") }
            MessageDataEntity(isPass = false, payload = argumentMessage.payload.toString())
        }

        campaignAddedEventListener.campaignAddedEvent().accept(campaigns)
    }

}