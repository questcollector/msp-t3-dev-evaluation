package com.samsung.sds.t3.dev.evaluation.repository.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import java.util.*

@Document(collection = "message_data")
data class MessageDataEntity (
    @Id
    val id: UUID? = null,
    val sentDateTime: LocalDateTime = LocalDateTime.now(),
    val instanceId: String? = null,
    val ipAddress: String? = null,
    val slackUserId: String? = null,
    val slackUserName: String? = null,
    val payload: String? = null,
    val isPass: Boolean = false,
    @Indexed(unique = true)
    val uuid: UUID? = null
) { }
