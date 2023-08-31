package com.samsung.sds.t3.dev.evaluation.event

import com.samsung.sds.t3.dev.evaluation.model.CampaignDTO
import com.samsung.sds.t3.dev.evaluation.service.MessageDataCommandService
import com.samsung.sds.t3.dev.evaluation.service.SlackMessagingService
import de.flapdoodle.embed.mongo.spring.autoconfigure.EmbeddedMongoAutoConfiguration
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.mockito.Mockito.doAnswer
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.cloud.stream.binder.test.InputDestination
import org.springframework.messaging.support.GenericMessage
import org.springframework.test.context.ActiveProfiles

@ExperimentalCoroutinesApi
@ActiveProfiles("test")
@SpringBootTest
@EnableAutoConfiguration(exclude= [EmbeddedMongoAutoConfiguration::class])
@ExtendWith(MockitoExtension::class)
class CampaignAddedEventBinderTests {
    @Autowired
    lateinit var inputDestination: InputDestination

    @MockBean
    lateinit var messageDataCommandService: MessageDataCommandService
    @MockBean
    lateinit var slackMessagingService: SlackMessagingService
    @MockBean
    lateinit var notificationEventPublisher: NotificationEventPublisher

    @SpyBean
    lateinit var campaignAddedEventListener: CampaignAddedEventListener

    @Test
    fun `campaignAddedEvent Binder test`() = runTest {
        val payload = CampaignDTO(campaignId = 1, campaignName = "name1")
        val inputMessage = GenericMessage(payload)
        val inputBinding = "campaignAddedEvent"

        doAnswer {
            val argument = it.getArgument(0) as GenericMessage<CampaignDTO>
            println(argument)
            assertThat(argument.payload).isEqualTo(inputMessage.payload)
        }.`when`(campaignAddedEventListener).handleMessage(any())


        inputDestination.send(inputMessage, inputBinding)

    }
    @Suppress("UNCHECKED_CAST")
    private fun <T> any(): T {
        Mockito.any<T>()
        return null as T
    }
}