package com.samsung.sds.t3.dev.evaluation.service

import com.samsung.sds.t3.dev.evaluation.repository.MessageDataRepository
import com.samsung.sds.t3.dev.evaluation.repository.entity.MessageDataEntity
import com.samsung.sds.t3.dev.evaluation.repository.entity.toMessageDataDTO
import com.samsung.sds.t3.dev.evaluation.service.MessageDataQueryServiceTests.Constant.NOW
import com.samsung.sds.t3.dev.evaluation.service.MessageDataQueryServiceTests.Constant.SAMPLE_UUID1
import com.samsung.sds.t3.dev.evaluation.service.MessageDataQueryServiceTests.Constant.SAMPLE_UUID2
import com.samsung.sds.t3.dev.evaluation.service.MessageDataQueryServiceTests.Constant.TODAY
import com.samsung.sds.t3.dev.evaluation.service.MessageDataQueryServiceTests.Constant.YESTERDAY
import io.mockk.coEvery
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.reactor.asFlux
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import reactor.test.StepVerifier
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*


private const val TEST = "test"


@ExtendWith(MockKExtension::class)
class MessageDataQueryServiceTests {

    private val messageDataRepository = mockk<MessageDataRepository>()

    private object Constant {
        val NOW: LocalDateTime = LocalDateTime.now().withNano(0)
        val TODAY: LocalDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
        val YESTERDAY: LocalDateTime = TODAY.minusDays(1)
        val SAMPLE_UUID1: UUID = UUID.randomUUID()
        val SAMPLE_UUID2: UUID = UUID.randomUUID()
    }

    @Test
    fun `특정 기간 동안 모든 메시지 조회`() {
        val expectEntities = listOf(
            MessageDataEntity(id = SAMPLE_UUID1, sentDateTime = TODAY.minusHours(1)),
            MessageDataEntity(id = SAMPLE_UUID2, sentDateTime = YESTERDAY.plusHours(1))
        )

        val fromRepository = flow {
            emitAll(expectEntities.asFlow())

            // not in expected result
            emit(MessageDataEntity(sentDateTime = TODAY.plusHours(1)))
            emit(MessageDataEntity(sentDateTime = YESTERDAY.minusHours(1)))
        }

        coEvery { messageDataRepository.findAll() } returns fromRepository

        val messageDataQueryService = MessageDataQueryService(messageDataRepository)

        runBlocking {
            val result = messageDataQueryService.getMessageDataDuring(
                YESTERDAY, TODAY
            )
            val expect = expectEntities.map { it.toMessageDataDTO() }

            StepVerifier.create(result.asFlux())
                .expectNext(expect[0], expect[1])
                .verifyComplete()
        }

    }

    @Test
    fun `기간 설정 없이 메시지 조회`() {
        val expectEntities = listOf(
            MessageDataEntity(id = SAMPLE_UUID1, sentDateTime = TODAY.minusHours(1))
        )

        val fromRepository = flow {
            emitAll(expectEntities.asFlow())

            // not in expected result
            emit(MessageDataEntity(id = SAMPLE_UUID2, sentDateTime = YESTERDAY.minusDays(1)))
        }

        coEvery { messageDataRepository.findAll() } returns fromRepository

        val messageDataQueryService = MessageDataQueryService(messageDataRepository)

        runBlocking {
            val result = messageDataQueryService.getMessageDataDuring()

            val expect = expectEntities.map { it.toMessageDataDTO() }

            StepVerifier.create(result.asFlux())
                .expectNext(expect[0])
                .verifyComplete()
        }

    }

    @Test
    fun `특정 유저의 모든 메시지 조회`() {

        val expectedEntities = listOf(
            MessageDataEntity(id = SAMPLE_UUID1, sentDateTime = NOW, slackUserName = "test1"),
            MessageDataEntity(id = SAMPLE_UUID2, sentDateTime = NOW, slackUserName = "test2")
        )

        coEvery { messageDataRepository.findAllBySlackUserNameStartsWith("test") } returns expectedEntities.asFlow()

        val messageDataQueryService = MessageDataQueryService(messageDataRepository)

        runBlocking {
            val result = messageDataQueryService.getMessageDataWithSlackUserName(TEST)

            val expect = expectedEntities.map { it.toMessageDataDTO() }

            StepVerifier.create(result.asFlux())
                .expectNext(expect[0], expect[1])
                .verifyComplete()
        }
    }

    @Test
    fun `특정 instanceId를 가진 모든 메시지 조회`() {
        val expectedEntities = listOf(
            MessageDataEntity(instanceId = TEST),
            MessageDataEntity(instanceId = TEST)
        )

        coEvery { messageDataRepository.findAllByInstanceId(TEST) } returns expectedEntities.asFlow()

        val messageDataQueryService = MessageDataQueryService(messageDataRepository)

        runBlocking {
            val result = messageDataQueryService.getMessageDataWithInstanceId(TEST)
            val expect = expectedEntities.map { it.toMessageDataDTO() }
            StepVerifier.create(result.asFlux())
                .expectNext(expect[0], expect[1])
                .verifyComplete()
        }
    }

    @Test
    fun `특정 UUID를 가진 메시지 조회`() {
        val expectedEntity = MessageDataEntity(id = SAMPLE_UUID1)

        coEvery { messageDataRepository.findById(SAMPLE_UUID1) } returns expectedEntity

        val messageDataQueryService = MessageDataQueryService(messageDataRepository)

        runBlocking {
            val result = messageDataQueryService.getMessageDataByMessageUuid(SAMPLE_UUID1.toString())
            assertThat(result).isEqualTo(expectedEntity.toMessageDataDTO())
        }
    }

    @Test
    fun `없는 메시지 조회`() = runBlocking {
        coEvery { messageDataRepository.findById(SAMPLE_UUID2) } returns null

        val messageDataQueryService = MessageDataQueryService(messageDataRepository)

        val result = messageDataQueryService.getMessageDataByMessageUuid(SAMPLE_UUID2.toString())
        assertThat(result).isNull()
    }

    @Test
    fun `잘못된 UUID로 조회`() = runBlocking {
        val messageDataQueryService = MessageDataQueryService(messageDataRepository)
        val result = messageDataQueryService.getMessageDataByMessageUuid("some-random-text")
        assertThat(result).isNull()
    }
}