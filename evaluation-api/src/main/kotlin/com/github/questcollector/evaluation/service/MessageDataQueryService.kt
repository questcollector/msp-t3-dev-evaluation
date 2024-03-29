package com.github.questcollector.evaluation.service

import com.github.questcollector.evaluation.model.MessageDataDTO
import com.github.questcollector.evaluation.repository.MessageDataRepository
import com.github.questcollector.evaluation.repository.entity.toMessageDataDTO
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

        return messageDataEntities
            .filterNotNull()
            .filter { it.sentDateTime in (start..< end) }
            .map { it.toMessageDataDTO() }
            .flowOn(Dispatchers.IO)
    }

    fun getMessageDataWithSlackUserName(slackUserName: String): Flow<MessageDataDTO> {
        log.info("getMessageDataWithSlackUserName invoked")
        val messageDataEntities = messageDataRepository.findAllBySlackUserNameStartsWith(slackUserName)

        return messageDataEntities
            .mapNotNull { it.toMessageDataDTO() }.flowOn(Dispatchers.IO)
    }

    fun getMessageDataWithInstanceId(instanceId: String): Flow<MessageDataDTO> {
        log.info("getMessageDataWithInstanceId invoked")
        val messageDataEntities = messageDataRepository.findAllByInstanceId(instanceId)

        return messageDataEntities
            .mapNotNull { it.toMessageDataDTO() }.flowOn(Dispatchers.IO)
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