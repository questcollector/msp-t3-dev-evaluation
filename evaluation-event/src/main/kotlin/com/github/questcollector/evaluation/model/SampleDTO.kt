package com.github.questcollector.evaluation.model

import java.time.OffsetDateTime

data class SampleDTO(
    val id : Int? = 0,
    val name: String? = null,
    val description: String? = null,
    val startDate: OffsetDateTime? = null,
    val endDate: OffsetDateTime? = null,
    val pictureUri: String? = null,
    val detailsUri: String? = null
)
