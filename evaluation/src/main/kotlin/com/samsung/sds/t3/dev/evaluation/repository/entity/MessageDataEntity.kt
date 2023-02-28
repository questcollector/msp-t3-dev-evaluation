package com.samsung.sds.t3.dev.evaluation.repository.entity

import com.samsung.sds.t3.dev.evaluation.model.MessageDataDTO
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*

@Document(collection = "message_data")
data class MessageDataEntity (
    @Id
    val id: ObjectId? = null,
    val sentDateTime: LocalDateTime? = null,
    val hostname: String? = null,
    val ipAddress: String? = null,
    val slackUserId: String? = null,
    val slackUserName: String? = null,
    val payload: String? = null,
    val isPass: Boolean = false,
    @Indexed(unique = true)
    val uuid: UUID? = null
) {
}

fun MessageDataEntity.toMessageDataDTO() = MessageDataDTO(
    uuid.toString(),
    sentDateTime?.run { OffsetDateTime.of(sentDateTime, ZoneOffset.ofHours(9)) },
    hostname,
    ipAddress,
    slackUserId,
    slackUserName,
    payload,
    isPass
)
