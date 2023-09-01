package com.samsung.sds.t3.dev.evaluation.repository.entity

import com.samsung.sds.t3.dev.evaluation.model.MessageDataDTO
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.*

@Document(collection = "message_data")
data class MessageDataEntity (
    @Id
    val id: UUID = UUID.randomUUID(),
    val sentDateTime: LocalDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
    val instanceId: String? = null,
    val ipAddress: String? = null,
    val slackUserId: String? = null,
    val slackUserName: String? = null,
    val payload: String? = null,
    val isPass: Boolean = false
)

fun MessageDataEntity.toMessageDataDTO() = MessageDataDTO(
    id.toString(),
    OffsetDateTime.of(sentDateTime, ZoneOffset.ofHours(9)),
    instanceId,
    ipAddress,
    slackUserId,
    slackUserName,
    payload,
    isPass
)
