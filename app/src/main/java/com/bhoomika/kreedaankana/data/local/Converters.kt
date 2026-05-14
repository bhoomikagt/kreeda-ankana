package com.bhoomika.kreedaankana.data.local

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalTime

class Converters {

    @TypeConverter
    fun fromDate(date: LocalDate?): String? = date?.toString()

    @TypeConverter
    fun toDate(date: String?): LocalDate? =
        date?.let { LocalDate.parse(it) }

    @TypeConverter
    fun fromTime(time: LocalTime?): String? = time?.toString()

    @TypeConverter
    fun toTime(time: String?): LocalTime? =
        time?.let { LocalTime.parse(it) }
}