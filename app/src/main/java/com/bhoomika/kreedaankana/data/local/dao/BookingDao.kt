package com.bhoomika.kreedaankana.data.local.dao

import androidx.room.*
import com.bhoomika.kreedaankana.data.local.entity.Booking
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

@Dao
interface BookingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: Booking)

    @Query("SELECT * FROM bookings WHERE date = :date")
    suspend fun getBookingsByDate(date: LocalDate): List<Booking>

    @Query("DELETE FROM bookings WHERE date = :date")
    suspend fun clearBookingsByDate(date: LocalDate)

    @Query("SELECT * FROM bookings WHERE groundId = :groundId")
    fun getBookingsForGround(groundId: String): Flow<List<Booking>>

    @Query("""
        SELECT * FROM bookings
        WHERE groundId = :groundId AND date = :date
        ORDER BY startTime
    """)
    fun getBookingsByGroundAndDate(
        groundId: String,
        date: LocalDate
    ): Flow<List<Booking>>
}