package com.github.questcollector.evaluation.event

import com.github.questcollector.evaluation.model.SampleDTO
import com.github.questcollector.evaluation.repository.entity.MessageDataEntity
import com.github.questcollector.evaluation.service.MessageDataCommandService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.Mockito
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
    fun `campaignAddedEvent Binder test`() {
        val payload = SampleDTO(id = 1, name = "name1")
        val inputMessage = GenericMessage(payload)
        val inputBinding = "campaignAddedEvent"

        runTest {
            given(messageDataCommandService.createMessageDataEntity(any()))
                .willAnswer {
                    val argument = it.getArgument(0) as GenericMessage<SampleDTO>
                    println(argument)
                    assertThat(argument.payload).isEqualTo(payload)
                    MessageDataEntity(isPass = false, payload = argument.payload.toString())
                }
        }

        inputDestination.send(inputMessage, inputBinding)

    }
    @Suppress("UNCHECKED_CAST")
    private fun <T> any(): T {
        Mockito.any<T>()
        return null as T
    }
}