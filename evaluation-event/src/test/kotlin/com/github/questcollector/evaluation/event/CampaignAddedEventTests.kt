package com.github.questcollector.evaluation.event

import com.github.questcollector.evaluation.event.validator.SampleDTOMessageValidator
import com.github.questcollector.evaluation.model.SampleDTO
import com.github.questcollector.evaluation.repository.entity.MessageDataEntity
import com.github.questcollector.evaluation.service.MessageDataCommandService
import com.github.questcollector.evaluation.service.SlackMessagingService
import io.mockk.*
import io.mockk.junit5.MockKExtension
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

        val messages = Flux.just(
            SampleDTO(id = 1, name = "name1"),
            SampleDTO(id = 2, name = "name2"),
            SampleDTO(id = 3, name = "name3")
        ).map {
            MessageBuilder.withPayload(it).build()
        }

        val validator = mockk<SampleDTOMessageValidator>()

        val campaignAddedEventListener = spyk(
            CampaignAddedEventListener(
                messageDataCommandService,
                slackMessagingService,
                notificationEventPublisher,
                validator
            )
        )

        // 무조건 통과
        coEvery { validator.validate(any()) } coAnswers {
            val argumentMessage = this.arg<GenericMessage<SampleDTO>>(0)
            MessageBuilder.fromMessage(argumentMessage).build()
        }

        // 이후 작업을 하지 않도록 MessageDataEntity 생성
        coEvery { messageDataCommandService.createMessageDataEntity(any()) } coAnswers {
            val argumentMessage = this.arg<GenericMessage<SampleDTO>>(0)
            MessageDataEntity(isPass = false, payload = argumentMessage.payload.toString())
        }

        campaignAddedEventListener.campaignAddedEvent().accept(messages)

        // 넘어온 argument로 messages가 잘 넘어왔는지 확인
        coVerify { messageDataCommandService.createMessageDataEntity(withArg {
            assertThat(it.payload)
                .matches { it.id in 1 ..< 3 }
                .matches { it.name in listOf("name1", "name2", "name3") }
        }) }
    }

    @Test
    fun `validator test`() {
        val messages = arrayOf(
            SampleDTO(id = 1, name = "name1"),
            SampleDTO(id = 0, name = "name0"),
            SampleDTO(id = 2, name = "name2"),
            SampleDTO(id = 3, name = " ")
        ).map { MessageBuilder.withPayload(it).build() }.toTypedArray()

        // 이후 작업을 하지 않도록 MessageDataEntity 생성
        coEvery { messageDataCommandService.createMessageDataEntity(any()) } coAnswers {
            val message = this.arg<GenericMessage<SampleDTO>>(0)
            MessageDataEntity(isPass = false, payload = message.payload.toString())
        }

        // validator는 그대로 사용
        val campaignAddedEventListener = spyk(
            CampaignAddedEventListener(
                messageDataCommandService,
                slackMessagingService,
                notificationEventPublisher,
                SampleDTOMessageValidator()
            )
        )


        campaignAddedEventListener.campaignAddedEvent().accept(Flux.just(*messages))

        // id가 양수가 아니고 name이 빈 값이 아닌 messages만 들어와야 함
        coVerify { messageDataCommandService.createMessageDataEntity(withArg {
            assertThat(it.payload)
                .matches { it.id !in listOf(0, 3) }
        }) }
    }
}