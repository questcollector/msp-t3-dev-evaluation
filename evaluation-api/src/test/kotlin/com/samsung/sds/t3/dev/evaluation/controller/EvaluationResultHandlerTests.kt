package com.samsung.sds.t3.dev.evaluation.controller

import com.samsung.sds.t3.dev.evaluation.config.EvaluationResultRouter
import com.samsung.sds.t3.dev.evaluation.model.EvaluationResultDTO
import com.samsung.sds.t3.dev.evaluation.model.SlackMemberVO
import com.samsung.sds.t3.dev.evaluation.service.EvaluationResultService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

private const val OK = "OK"

private const val TEST = "test"

@ExperimentalCoroutinesApi
@WebFluxTest(EvaluationResultHandler::class)
@ActiveProfiles(TEST)
@AutoConfigureWebTestClient
@ContextConfiguration(classes = [EvaluationResultHandler::class, EvaluationResultRouter::class])
class EvaluationResultHandlerTests {

    @MockBean
    private lateinit var evaluationResultService: EvaluationResultService
    @Autowired
    private lateinit var wtc: WebTestClient

    @Test
    fun `slackUserId 안 보냈을 경우`() {
        wtc.get().uri("/api/evaluation/slackUserId/")
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `startDate, endDate 안 보냈을 경우`() {
        val result = EvaluationResultDTO(true, OK, emptyList())
        runTest {
            given(evaluationResultService.getEvaluationResultBySlackUserId(
                TEST, LocalDateTime.MIN, LocalDateTime.MAX))
                .willReturn(result)
        }
        wtc.get().uri {
            it.path("/api/evaluation/slackUserId/")
                .queryParam("slackUserId", TEST)
                .build()
        }
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.result").isEqualTo(true)
            .jsonPath("$.reason").isEqualTo(OK)
    }

    @Test
    fun `이상한 startDate, endDate 보냈을 경우`() {
        val result = EvaluationResultDTO(true, OK, emptyList())
        runTest {
            given(evaluationResultService.getEvaluationResultBySlackUserId(
                TEST, LocalDateTime.MIN, LocalDateTime.MAX))
                .willReturn(result)
        }

        wtc.get().uri {
            it.path("/api/evaluation/slackUserId/")
                .queryParam("slackUserId", TEST)
                .queryParam("startDate", "strange-string")
                .queryParam("endDate", "strange-string")
                .build()
        }
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.result").isEqualTo(true)
            .jsonPath("$.reason").isEqualTo(OK)
    }

    @Test
    fun `모든 파라미터를 잘 보낸 경우`() {
        val startDate = "2023-03-06T00:00:00"
        val endDate = "2023-03-06T00:00:00"
        val startDateTime = LocalDateTime.parse(startDate)
        val endDateTime = LocalDateTime.parse(endDate)
        val result = EvaluationResultDTO(true, OK, emptyList())
        runTest {
            given(evaluationResultService.getEvaluationResultBySlackUserId(
                TEST, startDateTime, endDateTime))
                .willReturn(result)
        }

        wtc.get().uri {
            it.path("/api/evaluation/slackUserId/")
                .queryParam("slackUserId", TEST)
                .queryParam("startDate", startDate)
                .queryParam("endDate", endDate)
                .build()
        }
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.result").isEqualTo(true)
            .jsonPath("$.reason").isEqualTo(OK)
    }

    // 사용자 정의 Mockito.any()
    @Suppress("UNCHECKED_CAST")
    private fun <T> any(): T {
        Mockito.any<T>()
        return null as T
    }
    @Test
    @FlowPreview
    fun `평가 결과 파일 요청`() {

        // sample request body
        val fileContent = "example.file.data".toByteArray()
        val fileName = "sample"

        // 중간 결과물 Flow<SlackMemeberVO>
        val slackMembers = arrayListOf(
            SlackMemberVO("Member", 1, "userid1", "name1", "name1", "OK"),
            SlackMemberVO("Member", 1, "userid2", "name2", "name2", "OK"),
            SlackMemberVO("Member", 1, "userid3", "name3", "name3", "OK")
        ).asFlow()

        val now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)

        // 최종 csv 형태의 Flow<ByteArray>
        val csv = flow {
            emit("userid,fullname,displayname,result_${now}\n".toByteArray())
            emitAll(slackMembers.map {
                "${it.userId},\"${it.fullname}\",\"${it.displayname}\",${it.result}\n".toByteArray()
            })
        }

        // Mocking
        given(evaluationResultService.readCsv(any()))
            .willReturn(slackMembers)
        given(evaluationResultService.getResults(slackMembers, LocalDateTime.MIN, LocalDateTime.MAX))
            .willReturn(slackMembers)
        given(evaluationResultService.writeCsv(slackMembers))
            .willReturn(csv)

        wtc.post().uri {
            it.path("/api/evaluation/overall/")
                .build()
        }
            .contentLength(fileContent.size.toLong())
            .contentType(MediaType.parseMediaType("text/csv"))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$fileName\"")
            .body(Flux.just(fileContent), ByteArray::class.java)
            .exchange()

            .expectStatus().isOk
            .expectHeader().contentType(MediaType.parseMediaType("text/csv"))
            .expectHeader().valueMatches(
                HttpHeaders.CONTENT_DISPOSITION,
                """attachment; filename="result_\d{4}-\d{2}-\d{2}T\d{2}:\d{2}(:\d{2})?.csv""""
            )
            .expectBody().consumeWith { response ->
                val responseBody = String(response.responseBody!!)
                println(responseBody)
            }
    }
}