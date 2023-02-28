package com.samsung.sds.t3.dev.evaluation.controller

import com.samsung.sds.t3.dev.evaluation.service.MessageDataQueryService
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.FlowPreview
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import java.time.LocalDateTime
import java.time.format.DateTimeParseException

@Component
@Tag(name = "MessageData", description = "the MessageData API")
class MessageDataHandler(
    private val messageDataQueryService: MessageDataQueryService
) {

    private val log : Logger = LoggerFactory.getLogger(this.javaClass)

    suspend fun getMessageDataByMessageUuid(request: ServerRequest): ServerResponse {
        val uuid = request.pathVariable("uuid")
        val result = messageDataQueryService.getMessageDataByMessageUuid(uuid)

        return if (result == null)
            ServerResponse.notFound().buildAndAwait()
        else
            ServerResponse.ok().json().bodyValueAndAwait(result)
    }

    @FlowPreview
    suspend fun getMessageDataList(request: ServerRequest): ServerResponse {
        val startDate = request.queryParamOrNull("startDate")
        val endDate = request.queryParamOrNull("endDate")
        var startDateTime: LocalDateTime? = null
        var endDateTime: LocalDateTime? = null

        try {
            startDate?.run {startDateTime = LocalDateTime.parse(startDate)}
            endDate?.run {endDateTime = LocalDateTime.parse(endDate)}
        } catch (e: DateTimeParseException) {
            return ServerResponse.badRequest()
                .header("errorMessage", e.message)
                .buildAndAwait()
        }

        val result = messageDataQueryService.getMessageDataDuring(
            startDateTime, endDateTime)

        return ServerResponse.ok().json().bodyAndAwait(result)
    }

    @FlowPreview
    suspend fun getMessageDataListBySlackUserName(request: ServerRequest) : ServerResponse {
        val slackUserName = request.pathVariable("slackUserName")
        val result =  messageDataQueryService.getMessageDataWithSlackUserName(slackUserName)
        return ServerResponse.ok().json().bodyAndAwait(result)
    }
}

