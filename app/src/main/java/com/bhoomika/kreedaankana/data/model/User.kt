package com.bhoomika.kreedaankana.data.model

data class User(
    val name: String = "",
    val email: String = "",
    val teams: List<String> = emptyList()
)