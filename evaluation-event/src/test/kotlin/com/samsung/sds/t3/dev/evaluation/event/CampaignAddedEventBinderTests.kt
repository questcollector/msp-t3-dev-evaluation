package com.samsung.sds.t3.dev.evaluation.event

import com.samsung.sds.t3.dev.evaluation.model.CampaignDTO
import com.samsung.sds.t3.dev.evaluation.repository.entity.MessageDataEntity
import com.samsung.sds.t3.dev.evaluation.service.MessageDataCommandService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.mockito.Mockito.doAnswer
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.cloud.stream.binder.test.InputDestination
import org.springframework.messaging.support.GenericMessage
import org.springframework.test.context.ActiveProfiles

@ExperimentalCoroutinesApi
@ActiveProfiles("test")
@SpringBootTest
@ExtendWith(MockitoExtension::class)
class CampaignAddedEventBinderTests {
    @Autowired
    lateinit var inputDestination: InputDestination

    @MockBean
    lateinit var messageDataCommandService: MessageDataCommandService

    @Test
    fun `campaignAddedEvent Binder test`() = runTest {
        val payload = CampaignDTO(campaignId = 1, campaignName = "name1")
        val inputMessage = GenericMessage(payload)
        val inputBinding = "campaignAddedEvent"

        doAnswer {
            val argument = it.getArgument(0) as GenericMessage<CampaignDTO>
            println(argument)
            assertThat(argument.payload).isEqualTo(payload)
            MessageDataEntity(isPass = false, payload = argument.payload.toString())
        }.`when`(messageDataCommandService).createMessageDataEntity(any())


        inputDestination.send(inputMessage, inputBinding)

    }
    @Suppress("UNCHECKED_CAST")
    private fun <T> any(): T {
        Mockito.any<T>()
        return null as T
    }
}