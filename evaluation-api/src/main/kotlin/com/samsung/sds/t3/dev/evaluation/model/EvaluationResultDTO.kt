package com.samsung.sds.t3.dev.evaluation.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

data class EvaluationResultDTO(
    @Schema(
        name = "result",
        example = "true",
        description = "true or false",
        required = false
    )
    @JsonProperty("result")
    val result: Boolean,

    @Schema(
        name = "reason",
        example = "reason for evaluation result",
        description = "there's no messages from user",
        required = false
    )
    @JsonProperty("reason")
    val reason: String?,

    @Schema(
        name = "data",
        description = "messages from user",
        required = false
    )
    @JsonProperty("data")
    val data: List<MessageDataDTO>
)
