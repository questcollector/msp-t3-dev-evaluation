package com.samsung.sds.t3.dev.evaluation.service

import com.samsung.sds.t3.dev.evaluation.model.EvaluationResultDTO
import com.samsung.sds.t3.dev.evaluation.model.MessageDataDTO
import com.samsung.sds.t3.dev.evaluation.repository.MessageDataRepository
import com.samsung.sds.t3.dev.evaluation.repository.entity.toMessageDataDTO
import kotlinx.coroutines.flow.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
class MessageDataQueryService (
    private val messageDataRepository: MessageDataRepository
){
    private val log : Logger = LoggerFactory.getLogger(this.javaClass)

    suspend fun getMessageDataDuring(startDateTime: LocalDateTime? = null,
                                      endDateTime: LocalDateTime? = null): Flow<MessageDataDTO> {
        val now = LocalDateTime.now()
        val start = startDateTime ?: now.minusDays(1)
        val end = endDateTime ?: now

        if (log.isDebugEnabled) log.debug("converted dateTime: \nstart: $start\nend: $end")

        val messageDataEntities = messageDataRepository.findAllBySentDateTimeBetween(start, end)

        return messageDataEntities.mapNotNull {
            entity -> entity.toMessageDataDTO()
        }
    }

    suspend fun getMessageDataWithSlackUserName(slackUserName: String): Flow<MessageDataDTO> {
        val messageDataEntities = messageDataRepository.findAllBySlackUserName(slackUserName)

        return messageDataEntities.mapNotNull {
                entity -> entity.toMessageDataDTO()
        }
    }

    suspend fun getMessageDataByMessageUuid(messageId: String) : MessageDataDTO?
    {
        return try {
            messageDataRepository.findByUuid(UUID.fromString(messageId))?.toMessageDataDTO()
        } catch (e: IllegalArgumentException) {
            log.info("uuid is not a valid form: $messageId")
            null
        }

    }


}