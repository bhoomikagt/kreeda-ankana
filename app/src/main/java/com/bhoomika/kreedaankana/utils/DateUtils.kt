package com.bhoomika.kreedaankana.utils

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

fun getNext7Days(): List<LocalDate> {
    return (0..6).map { LocalDate.now().plusDays(it.toLong()) }
}

fun formatTime(start: LocalTime, end: LocalTime): String {
    val formatter = DateTimeFormatter.ofPattern("hh:mm a")
    return "${start.format(formatter)} - ${end.format(formatter)}"
}