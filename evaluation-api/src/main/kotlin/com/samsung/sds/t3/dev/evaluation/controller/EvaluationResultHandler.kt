package com.samsung.sds.t3.dev.evaluation.controller

import com.samsung.sds.t3.dev.evaluation.service.EvaluationResultService
import com.samsung.sds.t3.dev.evaluation.service.writeCsv
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactor.flux
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import reactor.kotlin.core.publisher.toMono
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.ByteBuffer
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

        val startDateTime = parseLocalDateTime(startDate, LocalDateTime.MIN)
        val endDateTime = parseLocalDateTime(endDate, LocalDateTime.MAX)

        val result = evaluationResultService.getEvaluationResultBySlackUserId(slackUserId, startDateTime, endDateTime)
        return ServerResponse.ok().json().bodyValueAndAwait(result)
    }

    suspend fun getEvaluationResultAsFile(request: ServerRequest) : ServerResponse {
        val startDate = request.queryParamOrNull("startDate")
        val endDate = request.queryParamOrNull("endDate")

        val startDateTime = parseLocalDateTime(startDate, LocalDateTime.MIN)
        val endDateTime = parseLocalDateTime(endDate, LocalDateTime.MAX)

        val fileBytes = request.bodyToFlow(ByteArray::class)
        val dataFrame = fileBytes.reduce { accumulator, value ->
            accumulator + value
        }

        val slackMemebers = evaluationResultService.readCsv(ByteArrayInputStream(dataFrame))
        val results = evaluationResultService.getResults(slackMemebers, startDateTime, endDateTime)

        val output = ByteArrayOutputStream().apply { writeCsv(results) }.toByteArray()
        val resource = ByteArrayResource(output)
        val now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)

        return ServerResponse.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .contentLength(resource.contentLength())
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment()
                    .filename("result_${now}.csv")
                    .build().toString())
            .bodyValueAndAwait(resource)
    }

    private fun parseLocalDateTime(datetime: String?, default: LocalDateTime): LocalDateTime {
        var result = default
        datetime?.run {
            try {
                result = LocalDateTime.parse(datetime)
            } catch (e : DateTimeParseException) {
                log.info("datetime parse error: ${datetime}")
            }
        }
        return result
    }
}