package com.bhoomika.kreedaankana.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

@Entity(
    tableName = "bookings",
    indices = [Index(value = ["bookingId"], unique = true)]
)
data class Booking(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val bookingId: String = "",

    val teamId: String,
    val teamName: String,

    val sport: String,

    val groundId: String,
    val groundName: String,

    val userId: String,

    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime
)