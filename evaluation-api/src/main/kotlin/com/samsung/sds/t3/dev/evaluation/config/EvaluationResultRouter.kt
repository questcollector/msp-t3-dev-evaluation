package com.samsung.sds.t3.dev.evaluation.config

import com.samsung.sds.t3.dev.evaluation.controller.EvaluationResultHandler
import com.samsung.sds.t3.dev.evaluation.model.EvaluationResultDTO
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springdoc.core.annotations.RouterOperation
import org.springdoc.core.annotations.RouterOperations
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.reactive.function.server.coRouter

@Configuration
class EvaluationResultRouter {
    @Bean
    @RouterOperations(*arrayOf(
        RouterOperation(
            method = arrayOf(RequestMethod.GET),
            beanClass = EvaluationResultHandler::class,
            beanMethod = "getEvaluationResultBySlackUserName",
            path = "/api/evaluation/slackUserName/",
            operation = Operation(
                operationId = "getEvaluationResultBySlackUserName",
                tags = ["Evaluation"],
                parameters = [
                    Parameter(
                        `in` = ParameterIn.QUERY,
                        name = "slackUserName",
                        description = "slack member Name like 홍길동",
                        required = true
                    ),
                    Parameter(
                        `in` = ParameterIn.QUERY,
                        name = "startDate",
                        description = "start LocalDateTime 2023-03-06T00:00:00, included",
                        required = false
                    ),
                    Parameter(
                        `in` = ParameterIn.QUERY,
                        name = "endDate",
                        description = "end LocalDateTime 2023-03-06T00:00:00, excluded",
                        required = false
                    )
                ],
                responses = [
                    ApiResponse(
                        responseCode = "200",
                        description = "Success",
                        content = [Content(mediaType = "application/json", schema = Schema(implementation = EvaluationResultDTO::class))]
                    )
                ]
            )
        )
    ))
    fun evaluationRoutes(evaluationResultHandler: EvaluationResultHandler) = coRouter {
        accept(MediaType.APPLICATION_JSON).nest {
            GET("/api/evaluation/slackUserName/", evaluationResultHandler::getEvaluationResultBySlackUserName)
        }
    }
}