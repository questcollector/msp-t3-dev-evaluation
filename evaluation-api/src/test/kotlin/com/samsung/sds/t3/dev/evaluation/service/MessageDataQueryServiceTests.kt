package com.samsung.sds.t3.dev.evaluation.service

import com.samsung.sds.t3.dev.evaluation.repository.MessageDataRepository
import com.samsung.sds.t3.dev.evaluation.repository.entity.MessageDataEntity
import com.samsung.sds.t3.dev.evaluation.repository.entity.toMessageDataDTO
import io.mockk.coEvery
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime


private const val TEST = "test"

@ExtendWith(MockKExtension::class)
class MessageDataQueryServiceTests {

    private val messageDataRepository = mockk<MessageDataRepository>()

    private val TODAY = LocalDateTime.now().withNano(0)
    private val YESTERDAY = TODAY.minusDays(1)

    @Test
    fun `특정 기간 동안 모든 메시지 조회`() {
        val entities = flow<MessageDataEntity> {
            emit(MessageDataEntity(sentDateTime = TODAY.minusHours(1)))
            emit(MessageDataEntity(sentDateTime = YESTERDAY.plusHours(1)))
        }

        coEvery { messageDataRepository.findAll() } returns entities

        val messageDataQueryService = MessageDataQueryService(messageDataRepository)

        runBlocking {
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
            emit(MessageDataEntity(sentDateTime = TODAY.minusHours(1)))
            emit(MessageDataEntity(sentDateTime = YESTERDAY.minusDays(1)))
        }

        coEvery { messageDataRepository.findAll() } returns entities

        val messageDataQueryService = MessageDataQueryService(messageDataRepository)

        runBlocking {
            val result = messageDataQueryService.getMessageDataDuring()
            assertThat(result.toList())
                .contains(entities.first().toMessageDataDTO())
                .doesNotContain(entities.last().toMessageDataDTO())
        }

    }

    @Test
    fun `특정 유저의 모든 메시지 조회`() {
        val entities = flow<MessageDataEntity> {
            emit(MessageDataEntity(slackUserName = "test"))
            emit(MessageDataEntity(slackUserName = "test"))
        }

        coEvery { messageDataRepository.findAllBySlackUserNameStartsWith("test") } returns entities

        val messageDataQueryService = MessageDataQueryService(messageDataRepository)

        runBlocking {
            val result = messageDataQueryService.getMessageDataWithSlackUserName(TEST)

            assertThat(result.toList()).containsAll(
                entities.map {
                        entity -> entity.toMessageDataDTO()
                }.toList()
            )
        }
    }
}