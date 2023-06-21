package com.samsung.sds.t3.dev.evaluation.controller

import com.samsung.sds.t3.dev.evaluation.config.EvaluationResultRouter
import com.samsung.sds.t3.dev.evaluation.model.EvaluationResultDTO
import com.samsung.sds.t3.dev.evaluation.model.SlackMemberVO
import com.samsung.sds.t3.dev.evaluation.service.EvaluationResultService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.*
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

private const val OK = "OK"

private const val TEST = "test"

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
    fun `slackuserid 안보냈을 경우`() {
        wtc.get().uri("/api/evaluation/slackUserId/")
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `startDate, endDate 안보냈을 경우`() {
        val result = EvaluationResultDTO(true, OK, emptyList())
        runBlocking {
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
        runBlocking {
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
        runBlocking {
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

    @Suppress("UNCHECKED_CAST")
    private fun <T> any(): T {
        Mockito.any<T>()
        return null as T
    }
    @Test
    fun `평가 결과 파일 요청`() {
        val fileContent = "example.file.data".toByteArray()
        val fileName = "sample"

        val flowContent = arrayListOf(
            SlackMemberVO("Member", 1, "userid1", "name1", "name1", "OK"),
            SlackMemberVO("Member", 1, "userid2", "name2", "name2", "OK"),
            SlackMemberVO("Member", 1, "userid3", "name3", "name3", "OK")
        )
        val slackMembers = flowContent.asFlow()

        runBlocking {
            given(evaluationResultService.readCsv(any()))
                .willReturn(slackMembers)
            given(evaluationResultService.getResults(slackMembers, LocalDateTime.MIN, LocalDateTime.MAX))
                .willReturn(slackMembers)
        }

        wtc.post().uri {
            it.path("/api/evaluation/overall/")
                .build()
        }
            .contentLength(fileContent.size.toLong())
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$fileName\"")
            .body(Flux.just(fileContent), ByteArray::class.java)
            .exchange()

            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_OCTET_STREAM)
            .expectHeader().valueMatches(
                HttpHeaders.CONTENT_DISPOSITION,
                """attachment; filename="result_\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}.csv""""
            )
            .expectBody().consumeWith { response ->
                val responseBody = String(response.responseBody!!)
                println(responseBody)
            }
    }
}