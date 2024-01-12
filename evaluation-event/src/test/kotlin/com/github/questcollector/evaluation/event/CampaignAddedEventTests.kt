package com.github.questcollector.evaluation.event

import com.github.questcollector.evaluation.model.SampleDTO
import com.github.questcollector.evaluation.repository.entity.MessageDataEntity
import com.github.questcollector.evaluation.service.MessageDataCommandService
import com.github.questcollector.evaluation.service.SlackMessagingService
import io.mockk.coEvery
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.spyk
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
    fun `consumer test`() {

        val campaigns = Flux.just(
            SampleDTO(id = 1, name = "name1"),
            SampleDTO(id = 2, name = "name2"),
            SampleDTO(id = 3, name = "name3")
        ).map {
            MessageBuilder.withPayload(it).build()
        }

        val campaignAddedEventListener = spyk(
            CampaignAddedEventListener(
            messageDataCommandService,
            slackMessagingService,
            notificationEventPublisher
        )
        )

        coEvery { messageDataCommandService.createMessageDataEntity(any()) } coAnswers {
            println("handleMessage invoked")
            val argumentMessage = this.arg<GenericMessage<SampleDTO>>(0)
            assertThat(argumentMessage.payload)
                .matches { it.id in 1 ..< 3 }
                .matches { it.name in listOf("name1", "name2", "name3") }
            MessageDataEntity(isPass = false, payload = argumentMessage.payload.toString())
        }

        campaignAddedEventListener.campaignAddedEvent().accept(campaigns)
    }

}