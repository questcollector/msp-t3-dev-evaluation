package com.samsung.sds.t3.dev.evaluation.controller

import com.samsung.sds.t3.dev.evaluation.service.EvaluationResultService
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import java.time.LocalDateTime
import java.time.format.DateTimeParseException

@Component
@Tag(name = "Evaluation", description = "the Evaluation API")
class EvaluationResultHandler(
    private val evaluationResultService: EvaluationResultService
) {
    private val log : Logger = LoggerFactory.getLogger(this.javaClass)
    suspend fun getEvaluationResultBySlackUserName(request: ServerRequest) : ServerResponse {
        val slackUserName = request.queryParamOrNull("slackUserName")
        val startDate = request.queryParamOrNull("startDate")
        val endDate = request.queryParamOrNull("endDate")
        if (log.isDebugEnabled) {
            log.debug("slackUserName: $slackUserName")
            log.debug("startDate: $startDate")
            log.debug("endDate: $endDate")
        }

        slackUserName ?: return ServerResponse.notFound().buildAndAwait()

        var startDateTime = LocalDateTime.MIN
        startDate?.run {
            try {
                startDateTime = LocalDateTime.parse(startDate)
            } catch (e : DateTimeParseException) {
                log.info("startDate parse error")
            }
        }
        var endDateTime = LocalDateTime.MAX
        endDate?.run {
            try {
                endDateTime = LocalDateTime.parse(endDate)
            } catch (e : DateTimeParseException) {
                log.info("endDate parse error")
            }
        }

        val result = evaluationResultService.getEvaluationResultBySlackUserName(slackUserName, startDateTime, endDateTime)
        return ServerResponse.ok().json().bodyValueAndAwait(result)
    }
}