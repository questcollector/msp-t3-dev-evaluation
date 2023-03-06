package com.samsung.sds.t3.dev.evaluation.repository

import com.samsung.sds.t3.dev.evaluation.repository.entity.MessageDataEntity
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*


@Repository
interface MessageDataRepository: CoroutineCrudRepository<MessageDataEntity, UUID> {
    fun findAllBySentDateTimeBetween(startDateTime: LocalDateTime, endDateTime: LocalDateTime): Flow<MessageDataEntity>
    fun findAllBySlackUserName(slackName: String) : Flow<MessageDataEntity>
    suspend fun findByUuid(uuid: UUID) : MessageDataEntity?

    fun findAllBySlackUserNameStartsWith(slackName: String) : Flow<MessageDataEntity>
}