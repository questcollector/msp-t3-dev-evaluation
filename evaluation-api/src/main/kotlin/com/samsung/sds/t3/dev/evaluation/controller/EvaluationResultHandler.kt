package com.samsung.sds.t3.dev.evaluation.controller

import com.samsung.sds.t3.dev.evaluation.service.EvaluationResultService
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*

@Component
@Tag(name = "Evaluation", description = "the Evaluation API")
class EvaluationResultHandler(
    private val evaluationResultService: EvaluationResultService
) {
    private val log : Logger = LoggerFactory.getLogger(this.javaClass)
    suspend fun getEvaluationResultBySlackUserName(request: ServerRequest) : ServerResponse {
        val slackUserName = request.queryParamOrNull("slackUserName")
        log.debug("slackUserName: $slackUserName")
        slackUserName ?: return ServerResponse.notFound().buildAndAwait()

        val result = evaluationResultService.getEvaluationResultBySlackUserName(slackUserName)
        return ServerResponse.ok().json().bodyValueAndAwait(result)
    }
}