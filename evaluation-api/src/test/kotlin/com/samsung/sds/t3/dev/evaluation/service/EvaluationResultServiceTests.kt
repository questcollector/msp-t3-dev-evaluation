package com.samsung.sds.t3.dev.evaluation.service

import com.samsung.sds.t3.dev.evaluation.model.EvaluationResultDTO
import com.samsung.sds.t3.dev.evaluation.model.SlackMemberVO
import com.samsung.sds.t3.dev.evaluation.repository.MessageDataRepository
import com.samsung.sds.t3.dev.evaluation.repository.entity.MessageDataEntity
import io.mockk.coEvery
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
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
            emit(MessageDataEntity(sentDateTime = TODAY, isPass = true, instanceId = "instanceId", ipAddress = "ipaddress"))
            emit(MessageDataEntity(sentDateTime = YESTERDAY.minusDays(1), isPass = true, instanceId = "instanceId", ipAddress = "ipaddress"))
        }

        coEvery { messageDataRepository.findAllBySlackUserId(TEST) } returns entities
        coEvery { messageDataRepository.findAllByInstanceId("instanceId") } returns entities

        val evaluationResultService = EvaluationResultService(messageDataRepository)

        runTest {
            val result = evaluationResultService.getEvaluationResultBySlackUserId(TEST, LocalDateTime.MIN, LocalDateTime.MAX)
            assertThat(result)
                .hasFieldOrPropertyWithValue("result", true)
                .hasFieldOrPropertyWithValue("reason", "OK")
            assertThat(result.data.size).isEqualTo(2)
        }
    }

    @Test
    fun `startDate, endDate 지정한 경우`() {
        val entities = flow<MessageDataEntity> {
            emit(MessageDataEntity(sentDateTime = TODAY, isPass = true, instanceId = "instanceId", ipAddress = "ipaddress"))
            emit(MessageDataEntity(sentDateTime = YESTERDAY, isPass = true, instanceId = "instanceId", ipAddress = "ipaddress"))
        }

        coEvery { messageDataRepository.findAllBySlackUserId(TEST) } returns entities
        coEvery { messageDataRepository.findAllByInstanceId("instanceId") } returns entities

        val evaluationResultService = EvaluationResultService(messageDataRepository)

        runTest {
            val result = evaluationResultService.getEvaluationResultBySlackUserId(TEST, YESTERDAY, TODAY)
            assertThat(result)
                .hasFieldOrPropertyWithValue("result", true)
                .hasFieldOrPropertyWithValue("reason", "OK")
            assertThat(result.data.size).isEqualTo(2)
        }
    }

    @Test
    fun `통과한 데이터 없는 경우`() {
        val entities = flow<MessageDataEntity> {
            emit(MessageDataEntity(sentDateTime = TODAY, instanceId = "instanceId", ipAddress = "ipaddress"))
            emit(MessageDataEntity(sentDateTime = YESTERDAY, instanceId = "instanceId", ipAddress = "ipaddress"))
        }
        coEvery { messageDataRepository.findAllBySlackUserId(TEST) } returns entities

        val evaluationResultService = EvaluationResultService(messageDataRepository)

        runTest {
            val result = evaluationResultService.getEvaluationResultBySlackUserId(TEST, YESTERDAY, TODAY)
            assertThat(result)
                .hasFieldOrPropertyWithValue("result", false)
                .hasFieldOrPropertyWithValue("reason", "통과한 메시지 없음")
            assertThat(result.data.size).isEqualTo(2)
        }
    }

    @Test
    fun `데이터 없는 경우`() {
        coEvery { messageDataRepository.findAllBySlackUserId(TEST) } returns emptyFlow()

        val evaluationResultService = EvaluationResultService(messageDataRepository)

        runTest {
            val result = evaluationResultService.getEvaluationResultBySlackUserId(TEST, YESTERDAY, TODAY)
            assertThat(result)
                .hasFieldOrPropertyWithValue("result", false)
                .hasFieldOrPropertyWithValue("reason", "메시지 없음")
            assertThat(result.data.size).isEqualTo(0)
        }
    }

    @Test
    fun `instanceId가 다른 경우`() {
        val entities = flow<MessageDataEntity> {
            emit(MessageDataEntity(sentDateTime = TODAY, isPass = true, instanceId = "instanceId", ipAddress = "ipaddress"))
            emit(MessageDataEntity(sentDateTime = YESTERDAY, isPass = true, instanceId = "instanceId2", ipAddress = "ipaddress"))
        }

        coEvery { messageDataRepository.findAllBySlackUserId(TEST) } returns entities

        val evaluationResultService = EvaluationResultService(messageDataRepository)

        runTest {
            val result = evaluationResultService.getEvaluationResultBySlackUserId(TEST, LocalDateTime.MIN, LocalDateTime.MAX)
            assertThat(result)
                .hasFieldOrPropertyWithValue("result", false)
                .hasFieldOrPropertyWithValue("reason", "복수의 VM에서 실행한 것으로 보임")
            assertThat(result.data.size).isEqualTo(2)
        }
    }

    @Test
    fun `ipAddress가 다른 경우`() {
        val entities = flow<MessageDataEntity> {
            emit(MessageDataEntity(sentDateTime = TODAY, isPass = true, instanceId = "instanceId", ipAddress = "ipaddress"))
            emit(MessageDataEntity(sentDateTime = YESTERDAY, isPass = true, instanceId = "instanceId", ipAddress = "ipaddress2"))
        }

        coEvery { messageDataRepository.findAllBySlackUserId(TEST) } returns entities

        val evaluationResultService = EvaluationResultService(messageDataRepository)

        runTest {
            val result = evaluationResultService.getEvaluationResultBySlackUserId(TEST, LocalDateTime.MIN, LocalDateTime.MAX)
            assertThat(result)
                .hasFieldOrPropertyWithValue("result", false)
                .hasFieldOrPropertyWithValue("reason", "복수의 VM에서 실행한 것으로 보임")
            assertThat(result.data.size).isEqualTo(2)
        }
    }

    @Test
    fun `하나의 인스턴스에서 여러 사람의 메시지가 보내진 경우`() {
        val entities = flow<MessageDataEntity> {
            emit(MessageDataEntity(sentDateTime = TODAY, isPass = true, instanceId = "instanceId", ipAddress = "ipaddress", slackUserId = "user1"))
            emit(MessageDataEntity(sentDateTime = YESTERDAY, isPass = true, instanceId = "instanceId", ipAddress = "ipaddress", slackUserId = "user2"))
        }

        coEvery { messageDataRepository.findAllBySlackUserId("user1") } returns flow { emit(entities.first()) }
        coEvery { messageDataRepository.findAllByInstanceId("instanceId") } returns entities

        val evaluationResultService = EvaluationResultService(messageDataRepository)

        runTest {
            val result = evaluationResultService.getEvaluationResultBySlackUserId("user1", LocalDateTime.MIN, LocalDateTime.MAX)
            assertThat(result)
                .hasFieldOrPropertyWithValue("result", false)
                .hasFieldOrPropertyWithValue("reason", "하나의 VM에서 여러 명이 실행한 것으로 보임")
            assertThat(result.data.size).isEqualTo(1)
        }
    }

    @Test
    fun `slack id 잘못 입력했을 경우 isCheated 값은 false`() {
        val entities = flow<MessageDataEntity> {
            emit(MessageDataEntity(sentDateTime = TODAY, isPass = false, instanceId = "instanceId", ipAddress = "ipaddress", slackUserId = "<<user>>"))
            emit(MessageDataEntity(sentDateTime = YESTERDAY, isPass = true, instanceId = "instanceId", ipAddress = "ipaddress", slackUserId = "user"))
        }
        coEvery { messageDataRepository.findAllByInstanceId("instanceId") } returns entities

        val evaluationResultService = EvaluationResultService(messageDataRepository)

        runTest {
            val result = evaluationResultService.isCheated("instanceId")
            assertThat(result).isEqualTo(false)
        }
    }

    @FlowPreview
    @Test
    fun `csv ByteArray를 SlackMemberVO로 변환`() {
        val csv = flow<ByteArray> {
            emit("username,email,status,billing-active,has-2fa,has-sso,userid,fullname,displayname,expiration-timestamp\n".toByteArray())
            emit("miroirs01,miroirs01@gmail.com,Member,1,1,0,U059H0Z4PH6,\"실습보조강사 유기영\"".toByteArray())
            emit(",\"실습보조강사 유기영\",\n".toByteArray())
        }
        val slackMember = SlackMemberVO(
            "Member",
            1,
            "U059H0Z4PH6",
            "실습보조강사 유기영",
            "실습보조강사 유기영"
        )

        val evaluationResultService = EvaluationResultService(messageDataRepository)

        runTest {
            val slackMembers = evaluationResultService.readCsv(csv)
            assertThat(slackMembers.first()).isEqualTo(slackMember)
        }
    }

    @Test
    fun `SlackMemberVO에서 결과 조회하기`() {
        val slackMember = SlackMemberVO(
            "Member",
            1,
            "U059H0Z4PH6",
            "실습보조강사 유기영",
            "실습보조강사 유기영"
        )

        val evaluationResultService = spyk(EvaluationResultService(messageDataRepository))

        coEvery {
            evaluationResultService.getEvaluationResultBySlackUserId("U059H0Z4PH6", LocalDateTime.MIN, LocalDateTime.MAX)
        } returns EvaluationResultDTO(true, "OK", emptyList())

        runTest {
            val slackMembers = evaluationResultService.getResults(flowOf(slackMember), LocalDateTime.MIN, LocalDateTime.MAX)
            assertThat(slackMembers.first().result).isEqualTo("OK")
        }
    }

    @FlowPreview
    @Test
    fun `csv 파일 쓰기`() {
        val slackMember = SlackMemberVO(
            "Member",
            1,
            "U059H0Z4PH6",
            "실습보조강사 유기영",
            "실습보조강사 유기영",
            "OK"
        )

        val evaluationResultService = EvaluationResultService(messageDataRepository)

        val csvHeader = "userid,fullname,displayname,result_\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}".toRegex()
        val csvRow = "${slackMember.userId},\"${slackMember.fullname}\",\"${slackMember.displayname}\",${slackMember.result}"

        runTest {
            val bytes = evaluationResultService.writeCsv(flowOf(slackMember))
                .reduce { accumulator, value ->  accumulator + value}
            val content = String(bytes, Charsets.UTF_8).split("\n")
            assertThat(content[0].matches(csvHeader)).isEqualTo(true)
            assertThat(content[1]).isEqualTo(csvRow)
        }
    }
}