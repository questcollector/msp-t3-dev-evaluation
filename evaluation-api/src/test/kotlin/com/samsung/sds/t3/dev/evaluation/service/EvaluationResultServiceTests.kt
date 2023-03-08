package com.samsung.sds.t3.dev.evaluation.service

import com.samsung.sds.t3.dev.evaluation.repository.MessageDataRepository
import com.samsung.sds.t3.dev.evaluation.repository.entity.MessageDataEntity
import io.mockk.coEvery
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime

private const val TEST = "test"

@ExtendWith(MockKExtension::class)
class EvaluationResultServiceTests {

    private val messageDataRepository = mockk<MessageDataRepository>()

    private val TODAY = LocalDateTime.now()
    private val YESTERDAY = TODAY.minusDays(1)

    @Test
    fun `startDate, endDate 지정하지 않은 상태`() {
        val entities = flow<MessageDataEntity> {
            emit(MessageDataEntity(sentDateTime = TODAY, isPass = true, hostname = "hostname", ipAddress = "ipaddress"))
            emit(MessageDataEntity(sentDateTime = YESTERDAY.minusDays(1), isPass = true, hostname = "hostname", ipAddress = "ipaddress"))
        }

        coEvery { messageDataRepository.findAllBySlackUserNameStartsWith(TEST) } returns entities

        val evaluationResultService = EvaluationResultService(messageDataRepository)

        runBlocking {
            val result = evaluationResultService.getEvaluationResultBySlackUserName(TEST, LocalDateTime.MIN, LocalDateTime.MAX)
            assertThat(result)
                .hasFieldOrPropertyWithValue("result", true)
                .hasFieldOrPropertyWithValue("reason", "OK")
            assertThat(result.data.size).isEqualTo(2)
        }
    }

    @Test
    fun `startDate, endDate 지정한 경우`() {
        val entities = flow<MessageDataEntity> {
            emit(MessageDataEntity(sentDateTime = TODAY, isPass = true, hostname = "hostname", ipAddress = "ipaddress"))
            emit(MessageDataEntity(sentDateTime = YESTERDAY, isPass = true, hostname = "hostname", ipAddress = "ipaddress"))
        }

        coEvery { messageDataRepository.findAllBySlackUserNameStartsWith(TEST) } returns entities

        val evaluationResultService = EvaluationResultService(messageDataRepository)

        runBlocking {
            val result = evaluationResultService.getEvaluationResultBySlackUserName(TEST, YESTERDAY, TODAY)
            assertThat(result)
                .hasFieldOrPropertyWithValue("result", true)
                .hasFieldOrPropertyWithValue("reason", "OK")
            assertThat(result.data.size).isEqualTo(2)
        }
    }

    @Test
    fun `통과한 데이터 없는 경우`() {
        val entities = flow<MessageDataEntity> {
            emit(MessageDataEntity(sentDateTime = TODAY, hostname = "hostname", ipAddress = "ipaddress"))
            emit(MessageDataEntity(sentDateTime = YESTERDAY, hostname = "hostname", ipAddress = "ipaddress"))
        }
        coEvery { messageDataRepository.findAllBySlackUserNameStartsWith(TEST) } returns entities

        val evaluationResultService = EvaluationResultService(messageDataRepository)

        runBlocking {
            val result = evaluationResultService.getEvaluationResultBySlackUserName(TEST, YESTERDAY, TODAY)
            assertThat(result)
                .hasFieldOrPropertyWithValue("result", false)
                .hasFieldOrPropertyWithValue("reason", "통과한 메시지 없음")
            assertThat(result.data.size).isEqualTo(2)
        }
    }

    @Test
    fun `데이터 없는 경우`() {
        coEvery { messageDataRepository.findAllBySlackUserNameStartsWith(TEST) } returns emptyFlow()

        val evaluationResultService = EvaluationResultService(messageDataRepository)

        runBlocking {
            val result = evaluationResultService.getEvaluationResultBySlackUserName(TEST, YESTERDAY, TODAY)
            assertThat(result)
                .hasFieldOrPropertyWithValue("result", false)
                .hasFieldOrPropertyWithValue("reason", "메시지 없음")
            assertThat(result.data.size).isEqualTo(0)
        }
    }

    @Test
    fun `hostname이 다른 경우`() {
        val entities = flow<MessageDataEntity> {
            emit(MessageDataEntity(sentDateTime = TODAY, isPass = true, hostname = "hostname", ipAddress = "ipaddress"))
            emit(MessageDataEntity(sentDateTime = YESTERDAY, isPass = true, hostname = "hostname2", ipAddress = "ipaddress"))
        }

        coEvery { messageDataRepository.findAllBySlackUserNameStartsWith(TEST) } returns entities

        val evaluationResultService = EvaluationResultService(messageDataRepository)

        runBlocking {
            val result = evaluationResultService.getEvaluationResultBySlackUserName(TEST, LocalDateTime.MIN, LocalDateTime.MAX)
            assertThat(result)
                .hasFieldOrPropertyWithValue("result", false)
                .hasFieldOrPropertyWithValue("reason", "다른 VM에서 실행한 것으로 보임")
            assertThat(result.data.size).isEqualTo(2)
        }
    }

    @Test
    fun `ipAddress가 다른 경우`() {
        val entities = flow<MessageDataEntity> {
            emit(MessageDataEntity(sentDateTime = TODAY, isPass = true, hostname = "hostname", ipAddress = "ipaddress"))
            emit(MessageDataEntity(sentDateTime = YESTERDAY, isPass = true, hostname = "hostname", ipAddress = "ipaddress2"))
        }

        coEvery { messageDataRepository.findAllBySlackUserNameStartsWith(TEST) } returns entities

        val evaluationResultService = EvaluationResultService(messageDataRepository)

        runBlocking {
            val result = evaluationResultService.getEvaluationResultBySlackUserName(TEST, LocalDateTime.MIN, LocalDateTime.MAX)
            assertThat(result)
                .hasFieldOrPropertyWithValue("result", false)
                .hasFieldOrPropertyWithValue("reason", "다른 VM에서 실행한 것으로 보임")
            assertThat(result.data.size).isEqualTo(2)
        }
    }
}