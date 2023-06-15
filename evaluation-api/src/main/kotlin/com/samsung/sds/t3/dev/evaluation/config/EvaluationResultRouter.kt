package com.samsung.sds.t3.dev.evaluation.config

import com.samsung.sds.t3.dev.evaluation.controller.EvaluationResultHandler
import com.samsung.sds.t3.dev.evaluation.model.EvaluationResultDTO
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
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
            beanMethod = "getEvaluationResultBySlackUserId",
            path = "/api/evaluation/slackUserId/",
            operation = Operation(
                operationId = "getEvaluationResultBySlackUserId",
                description = "experimental",
                tags = ["Evaluation"],
                parameters = [
                    Parameter(
                        `in` = ParameterIn.QUERY,
                        name = "slackUserId",
                        description = "slack member id like UXXXXXXXXXX",
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
        ),
        RouterOperation(
            method = arrayOf(RequestMethod.POST),
            beanClass = EvaluationResultHandler::class,
            beanMethod = "getEvaluationResultAsFile",
            path = "/api/evaluation/overall/",
            operation = Operation(
                operationId = "getEvaluationResultAsFile",
                tags = ["Evaluation"],
                parameters = [
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
                requestBody = RequestBody(
                    description = "수강생 명단 목록 csv 파일",
                    content = [Content(mediaType = "application/octet-stream",
                        schema = Schema(type = "string", format = "binary"))],
                    required = true
                ),
                responses = [
                    ApiResponse(
                        responseCode = "200",
                        description = "Success",
                        content = [Content(mediaType = "application/octet-stream",
                            schema = Schema(type = "string", format = "binary"))]
                    )
                ]
            )
        )
    ))
    fun evaluationRoutes(evaluationResultHandler: EvaluationResultHandler) = coRouter {
        accept(MediaType.APPLICATION_JSON).nest {
            GET("/api/evaluation/slackUserId/", evaluationResultHandler::getEvaluationResultBySlackUserId)
        }
        accept(MediaType.APPLICATION_OCTET_STREAM).nest {
            POST("/api/evaluation/overall/", evaluationResultHandler::getEvaluationResultAsFile)
        }
    }
}