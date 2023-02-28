package com.samsung.sds.t3.dev.evaluation.model

import java.time.OffsetDateTime

data class CampaignDTO(
    val campaignId : Int? = 0,
    val campaignName: String? = null,
    val description: String? = null,
    val startDate: OffsetDateTime? = null,
    val endDate: OffsetDateTime? = null,
    val pictureUri: String? = null,
    val detailsUri: String? = null
)
