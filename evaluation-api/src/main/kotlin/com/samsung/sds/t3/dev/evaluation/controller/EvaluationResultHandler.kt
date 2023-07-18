package com.samsung.sds.t3.dev.evaluation.controller

import com.samsung.sds.t3.dev.evaluation.service.EvaluationResultService
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.FlowPreview
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import java.time.LocalDateTime
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit

@Component
@Tag(name = "Evaluation", description = "the Evaluation API")
class EvaluationResultHandler(
    private val evaluationResultService: EvaluationResultService
) {
    private val log : Logger = LoggerFactory.getLogger(this.javaClass)
    suspend fun getEvaluationResultBySlackUserId(request: ServerRequest) : ServerResponse {
        val slackUserId = request.queryParamOrNull("slackUserId")
        val startDate = request.queryParamOrNull("startDate")
        val endDate = request.queryParamOrNull("endDate")
        if (log.isDebugEnabled) {
            log.debug("slackUserId: $slackUserId")
            log.debug("startDate: $startDate")
            log.debug("endDate: $endDate")
        }

        slackUserId ?: return ServerResponse.notFound().buildAndAwait()

        val startDateTime = startDate.parseLocalDateTimeWithDefaultValue(LocalDateTime.MIN)
        val endDateTime = endDate.parseLocalDateTimeWithDefaultValue(LocalDateTime.MAX)

        val result = evaluationResultService.getEvaluationResultBySlackUserId(slackUserId, startDateTime, endDateTime)
        return ServerResponse.ok().json().bodyValueAndAwait(result)
    }

    @FlowPreview
    suspend fun getEvaluationResultAsFile(request: ServerRequest) : ServerResponse {

        // parameter 변환
        val startDate = request.queryParamOrNull("startDate")
        val endDate = request.queryParamOrNull("endDate")

        val startDateTime = startDate.parseLocalDateTimeWithDefaultValue(LocalDateTime.MIN)
        val endDateTime = endDate.parseLocalDateTimeWithDefaultValue(LocalDateTime.MAX)

        // file content Flow<ByteArray>로 변환
        val fileBytes = request.bodyToFlow(ByteArray::class)

        // SlackMemberVO 변환, 결과 처리
        val slackMembers = evaluationResultService.readCsv(fileBytes)
        val results = evaluationResultService.getResults(slackMembers, startDateTime, endDateTime)

        // CSV 형태로 Flow<ByteArray> 생성
        val outputCsv = evaluationResultService.writeCsv(results)

        val now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)

        return ServerResponse.ok()
            .contentType(MediaType.parseMediaType("text/csv"))
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment()
                    .filename("result_${now}.csv")
                    .build().toString())
            .bodyAndAwait(outputCsv)
    }

}

fun String?.parseLocalDateTimeWithDefaultValue(default: LocalDateTime): LocalDateTime {
    var result = default
    this?.run {
        try {
            result = LocalDateTime.parse(this)
        } catch (e : DateTimeParseException) {
            println("datetime parse error: ${this}")
        }
    }
    return result
}