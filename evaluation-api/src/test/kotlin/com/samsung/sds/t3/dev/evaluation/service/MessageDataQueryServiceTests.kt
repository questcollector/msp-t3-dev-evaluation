package com.samsung.sds.t3.dev.evaluation.service

import com.samsung.sds.t3.dev.evaluation.repository.MessageDataRepository
import com.samsung.sds.t3.dev.evaluation.repository.entity.MessageDataEntity
import com.samsung.sds.t3.dev.evaluation.repository.entity.toMessageDataDTO
import io.mockk.coEvery
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*


private const val TEST = "test"

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class MessageDataQueryServiceTests {

    private val messageDataRepository = mockk<MessageDataRepository>()

    private val TODAY = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
    private val YESTERDAY = TODAY.minusDays(1)
    private val SAMPLE_UUID1 = UUID.randomUUID()
    private val SAMPLE_UUID2 = UUID.randomUUID()

    @Test
    fun `특정 기간 동안 모든 메시지 조회`() {
        val entities = flow<MessageDataEntity> {
            emit(MessageDataEntity(id = SAMPLE_UUID1, sentDateTime = TODAY.minusHours(1)))
            emit(MessageDataEntity(id = SAMPLE_UUID2, sentDateTime = YESTERDAY.plusHours(1)))
        }

        coEvery { messageDataRepository.findAll() } returns entities

        val messageDataQueryService = MessageDataQueryService(messageDataRepository)

        runTest {
            val result = messageDataQueryService.getMessageDataDuring(
                YESTERDAY, TODAY
            )
            println(result.toList())

            assertThat(result.toList()).containsAll(
                entities.map {
                        entity -> entity.toMessageDataDTO()
                }.toList()
            )
        }

    }

    @Test
    fun `기간 설정 없이 메시지 조회`() {
        val entities = flow<MessageDataEntity> {
            emit(MessageDataEntity(id = SAMPLE_UUID1, sentDateTime = TODAY.minusHours(1)))
            emit(MessageDataEntity(id = SAMPLE_UUID2, sentDateTime = YESTERDAY.minusDays(1)))
        }

        coEvery { messageDataRepository.findAll() } returns entities

        val messageDataQueryService = MessageDataQueryService(messageDataRepository)

        runTest {
            val result = messageDataQueryService.getMessageDataDuring()
            assertThat(result.toList())
                .contains(entities.first().toMessageDataDTO())
                .doesNotContain(entities.last().toMessageDataDTO())
        }

    }

    @Test
    fun `특정 유저의 모든 메시지 조회`() {
        val NOW = LocalDateTime.now().withNano(0)
        val entities = flow<MessageDataEntity> {
            emit(MessageDataEntity(id = SAMPLE_UUID1, sentDateTime = NOW, slackUserName = "test"))
            emit(MessageDataEntity(id = SAMPLE_UUID2, sentDateTime = NOW, slackUserName = "test"))
        }

        coEvery { messageDataRepository.findAllBySlackUserNameStartsWith("test") } returns entities

        val messageDataQueryService = MessageDataQueryService(messageDataRepository)

        runTest {
            val result = messageDataQueryService.getMessageDataWithSlackUserName(TEST)

            assertThat(result.toList()).containsAll(
                entities.map {
                        entity -> entity.toMessageDataDTO()
                }.toList()
            )
        }
    }
}