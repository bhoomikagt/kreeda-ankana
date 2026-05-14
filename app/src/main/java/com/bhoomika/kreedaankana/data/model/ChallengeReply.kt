package com.bhoomika.kreedaankana.data.model

data class ChallengeReply(

    val id: String = "",

    val challengeId: String = "",

    val teamId: String = "",
    val teamName: String = "",

    val message: String = "",

    val status: String = "PENDING",

    val createdAt: Long =
        System.currentTimeMillis()
)