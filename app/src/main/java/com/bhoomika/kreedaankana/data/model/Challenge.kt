package com.bhoomika.kreedaankana.data.model

data class Challenge(

    val id: String = "",

    val bookingId: String = "",

    val hostUserId: String = "",
    val hostTeamId: String = "",
    val hostTeamName: String = "",

    val sport: String = "",

    val groundId: String = "",
    val groundName: String = "",

    val date: String = "",
    val startTime: String = "",
    val endTime: String = "",

    val description: String = "",

    val status: String = "OPEN",

    val acceptedTeamId: String? = null,
    val acceptedTeamName: String? = null,

    val createdAt: Long = System.currentTimeMillis()
)