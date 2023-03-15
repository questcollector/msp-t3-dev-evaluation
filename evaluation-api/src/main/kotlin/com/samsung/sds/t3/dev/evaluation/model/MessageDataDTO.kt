package com.samsung.sds.t3.dev.evaluation.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.format.annotation.DateTimeFormat
import java.time.OffsetDateTime
import java.util.*

/**
 * MessageDataDTO
 */
data class MessageDataDTO (
    /**
     * message id: UUID
     * @return messageId
     */
    @Schema(
    name = "messageId",
    example = "6bcd5858-6786-4d93-9d3e-d93785e9e05c",
    description = "message id: UUID",
    required = false
    )
    @JsonProperty("messageId")
    val messageId: String? = null,

    /**
     * datetime that a message sent
     * @return sentDateTime
     */
    @Schema(
        name = "sentDateTime",
        example = "2022-05-18T05:01:43+09:00",
        description = "datetime that a message sent",
        required = false
    )
    @JsonProperty("sentDateTime")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    val sentDateTime: OffsetDateTime? = null,

    /**
     * hostname that a message came from
     * @return hostname
     */
    @Schema(
        name = "instanceId",
        example = "i-xxxxxxxxxxxxxxxxx",
        description = "instanceId that a message came from",
        required = false
    )
    @JsonProperty("instanceId")
    val instanceId: String? = null,

    /**
     * ipAddress that a message came from
     * @return ipAddress
     */
    @Schema(
        name = "ipAddress",
        example = "172.31.XXX.XXX",
        description = "ipAddress that a message came from",
        required = false
    )
    @JsonProperty("ipAddress")
    val ipAddress: String? = null,

    /**
     * slack member id
     * @return slackUserId
     */
    @Schema(
        name = "slackUserId",
        example = "UXXXXXXXXXX",
        description = "slack member id",
        required = false
    )
    @JsonProperty("slackUserId")
    val slackUserId: String? = null,

    /**
     * slack member name
     * @return slackUserName
     */
    @Schema(
        name = "slackUserName",
        example = "홍길동(hong.gildong)",
        description = "slack member name",
        required = false
    )
    @JsonProperty("slackUserName")
    val slackUserName: String? = null,

    /**
     * message payload(CampaignDTO)
     * @return payload
     */
    @Schema(
        name = "payload",
        example = "{\"campaignId\":23,...}",
        description = "message payload(CampaignDTO)",
        required = false
    )
    @JsonProperty("payload")
    val payload: String? = null,

    /**
     * a message is good enough or not
     * @return isPass
     */
    @Schema(
        name = "isPass",
        example = "true",
        description = "a message is good enough or not",
        required = false
    )
    @JsonProperty("isPass")
    val isPass: Boolean = false
) {}