package com.github.questcollector.evaluation.model

data class SlackMemberVO(
    val status: String,
    val billingActive: Int,
    val userId: String,
    val fullname: String,
    val displayname: String,
    var result: String = ""
)
