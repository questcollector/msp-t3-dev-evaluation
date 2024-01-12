package com.github.questcollector.evaluation.model

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import java.time.OffsetDateTime

data class SampleDTO(
    @Positive
    val id : Int,
    @NotBlank
    val name: String,
    val description: String? = null,
    val startDate: OffsetDateTime? = null,
    val endDate: OffsetDateTime? = null,
    val pictureUri: String? = null,
    val detailsUri: String? = null
)
