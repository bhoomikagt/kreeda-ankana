package com.bhoomika.kreedaankana.data.model

data class MatchResult(

    val id: String = "",

    val challengeId: String = "",

    val sport: String = "",

    // Host Team
    val hostTeamId: String = "",
    val hostTeamName: String = "",

    // Opponent Team
    val opponentTeamId: String = "",
    val opponentTeamName: String = "",

    // Scores
    val hostScore: Int = 0,
    val opponentScore: Int = 0,

    // Winner
    val winnerTeamId: String = "",
    val winnerTeamName: String = "",

    // Match summary
    val summary: String = "",

    // Metadata
    val groundName: String = "",
    val date: String = "",

    val postedBy: String = "",

    val createdAt: Long =
        System.currentTimeMillis()
)