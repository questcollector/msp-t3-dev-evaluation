package com.samsung.sds.t3.dev.evaluation.service

import com.samsung.sds.t3.dev.evaluation.model.MessageDataDTO
import com.samsung.sds.t3.dev.evaluation.repository.MessageDataRepository
import com.samsung.sds.t3.dev.evaluation.repository.entity.toMessageDataDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
class MessageDataQueryService(
    private val messageDataRepository: MessageDataRepository
) {
    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    fun getMessageDataDuring(
        startDateTime: LocalDateTime? = null,
        endDateTime: LocalDateTime? = null
    ): Flow<MessageDataDTO> {

        log.info("getMessageDataDuring invoked")

        val now = LocalDateTime.now().withNano(0)
        val start = startDateTime ?: now.minusDays(1)
        val end = endDateTime ?: now

        val messageDataEntities = messageDataRepository.findAll()

        return messageDataEntities.flowOn(Dispatchers.IO)
            .filterNotNull()
            .filter { it.sentDateTime.isAfter(start) && it.sentDateTime.isBefore(end) }
            .map { entity -> entity.toMessageDataDTO() }
    }

    fun getMessageDataWithSlackUserName(slackUserName: String): Flow<MessageDataDTO> {
        log.info("getMessageDataWithSlackUserName invoked")
        val messageDataEntities = messageDataRepository.findAllBySlackUserNameStartsWith(slackUserName)

        return messageDataEntities.flowOn(Dispatchers.IO)
            .mapNotNull { entity ->
                entity.toMessageDataDTO()
            }
    }

    fun getMessageDataWithInstanceId(instanceId: String): Flow<MessageDataDTO> {
        log.info("getMessageDataWithInstanceId invoked")
        val messageDataEntities = messageDataRepository.findAllByInstanceId(instanceId)

        return messageDataEntities.flowOn(Dispatchers.IO)
            .mapNotNull { entity ->
                entity.toMessageDataDTO()
            }
    }

    suspend fun getMessageDataByMessageUuid(messageId: String): MessageDataDTO? {
        log.info("getMessageDataByMessageUuid invoked")
        return try {
            messageDataRepository.findById(UUID.fromString(messageId))?.toMessageDataDTO()
        } catch (e: IllegalArgumentException) {
            log.info("uuid is not a valid form: $messageId")
            null
        }
    }
}