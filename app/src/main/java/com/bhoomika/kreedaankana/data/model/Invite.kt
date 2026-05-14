package com.bhoomika.kreedaankana.data.model

data class Invite(
    val id: String = "",

    val hostTeamId: String = "",
    val hostTeamName: String = "",

    val opponentTeamId: String = "",
    val opponentTeamName: String = "",

    val sport: String = "",

    val date: String = "",
    val startTime: String = "",
    val endTime: String = "",

    val message: String = "",
    val status: String = "open"
)