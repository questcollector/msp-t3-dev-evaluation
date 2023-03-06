package com.samsung.sds.t3.dev.evaluation.config

import com.samsung.sds.t3.dev.evaluation.controller.MessageDataHandler
import com.samsung.sds.t3.dev.evaluation.model.MessageDataDTO
import com.samsung.sds.t3.dev.evaluation.model.EvaluationResultDTO
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.models.info.Info
import kotlinx.coroutines.FlowPreview
import org.springdoc.core.annotations.RouterOperation
import org.springdoc.core.annotations.RouterOperations
import org.springdoc.core.models.GroupedOpenApi
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.reactive.function.server.coRouter

@Configuration
class MessageDataRouter {

    @Bean
    fun messageDataOpenApi(@Value("\${springdoc.version:v1}") appVersion: String? = "v1"): GroupedOpenApi? {
        val messageDataPaths = "/api/messageData/**"
        val evaluationDataPaths = "/api/evaluation/**"
        return GroupedOpenApi.builder().group("messageData")
            .addOpenApiCustomizer { openApi -> openApi.info(Info().title("messageData API").version(appVersion)) }
            .pathsToMatch(messageDataPaths, evaluationDataPaths)
            .build()
    }

    @FlowPreview
    @Bean
    @RouterOperations(*arrayOf(
        RouterOperation(
            method = arrayOf(RequestMethod.GET),
            beanClass = MessageDataHandler::class,
            beanMethod = "getMessageDataByMessageUuid",
            path = "/api/messageData/uuid/{uuid}",
            operation = Operation(
                operationId = "getMessageDataByMessageUuid",
                tags = ["MessageData"],
                parameters = [
                    Parameter(
                        `in` = ParameterIn.PATH,
                        name = "uuid",
                        description = "message id: UUID",
                        required = true
                    )
                             ],
                responses = [
                    ApiResponse(
                        responseCode = "200",
                        description = "Success",
                        content = [
                            Content(mediaType = "application/json",
                                schema = Schema(implementation = MessageDataDTO::class))
                        ]
                    ),
                    ApiResponse(responseCode = "404", description = "Not Found")]
            )
        ),
        RouterOperation(
            method = arrayOf(RequestMethod.GET),
            beanClass = MessageDataHandler::class,
            beanMethod = "getMessageDataList",
            path = "/api/messageData/",
            operation = Operation(
                operationId = "getMessageDataList",
                tags = ["MessageData"],
                parameters = [
                    Parameter(
                        `in` = ParameterIn.QUERY,
                        name = "startDate",
                        description = "start LocalDateTime 2023-03-06T00:00:00",
                        required = false
                    ),
                    Parameter(
                        `in` = ParameterIn.QUERY,
                        name = "endDate",
                        description = "end LocalDateTime 2023-03-06T00:00:00",
                        required = false
                    )
                             ],
                responses = [
                    ApiResponse(
                        responseCode = "200",
                        description = "Success",
                        content = [Content(mediaType = "application/json", schema = Schema(implementation = MessageDataDTO::class))]
                    ),
                    ApiResponse(responseCode = "400", description = "Bad Request")
                ]
            )
        ),
        RouterOperation(
            method = arrayOf(RequestMethod.GET),
            beanClass = MessageDataHandler::class,
            beanMethod = "getMessageDataListBySlackUserName",
            path = "/api/messageData/slackUserName/{slackUserName}",
            operation = Operation(
                operationId = "getMessageDataListBySlackUserName",
                tags = ["MessageData"],
                parameters = [
                    Parameter(
                        `in` = ParameterIn.PATH,
                        name = "slackUserName",
                        description = "slack member Name 홍길동(hong.gildong)",
                        required = true
                    )
                             ],
                responses = [ApiResponse(
                    responseCode = "200",
                    description = "Success",
                    content = [Content(mediaType = "application/json", schema = Schema(implementation = MessageDataDTO::class))]
                )]
            )
        ),
        RouterOperation(
            method = arrayOf(RequestMethod.GET),
            beanClass = MessageDataHandler::class,
            beanMethod = "getEvaluationResultBySlackUserName",
            path = "/api/evaluation/slackUserName/",
            operation = Operation(
                operationId = "getEvaluationResultBySlackUserName",
                tags = ["MessageData"],
                parameters = [
                    Parameter(
                        `in` = ParameterIn.QUERY,
                        name = "slackUserName",
                        description = "slack member Name like 홍길동",
                        required = true
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
    fun messageDataRoutes(messageDataHandler: MessageDataHandler) = coRouter {
        accept(MediaType.APPLICATION_JSON).nest {
            GET("/api/messageData/", messageDataHandler::getMessageDataList)
            GET("/api/messageData/slackUserName/{slackUserName}", messageDataHandler::getMessageDataListBySlackUserName)
            GET("/api/messageData/uuid/{uuid}", messageDataHandler::getMessageDataByMessageUuid)
            GET("/api/evaluation/slackUserName/", messageDataHandler::getEvaluationResultBySlackUserName)
        }
    }
}