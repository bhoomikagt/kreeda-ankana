package com.bhoomika.kreedaankana.repository

import android.util.Log
import com.bhoomika.kreedaankana.data.local.dao.BookingDao
import com.bhoomika.kreedaankana.data.local.entity.Booking
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
class BookingRepository(
    private val dao: BookingDao,
    private val firestore: FirebaseFirestore
) {

    // ===============================
    // 🔥 BOOK SLOT (WITH CONFLICT CHECK)
    // ===============================
    suspend fun bookSlot(booking: Booking): String {

        val bookingsRef = firestore.collection("bookings")

        val snapshot = bookingsRef
            .whereEqualTo("date", booking.date.toString())
            .whereEqualTo("groundId", booking.groundId)
            .get()
            .await()

        val conflict = snapshot.documents.firstOrNull {

            val start = LocalTime.parse(it.getString("startTime"))
            val end = LocalTime.parse(it.getString("endTime"))

            !(end <= booking.startTime || start >= booking.endTime)
        }

        if (conflict != null) {
            val start = conflict.getString("startTime")
            val end = conflict.getString("endTime")
            return "Slot overlaps with ($start - $end)"
        }

        val doc = bookingsRef.add(
            mapOf(
                "teamId" to booking.teamId,
                "teamName" to booking.teamName,

                "sport" to booking.sport,

                "groundId" to booking.groundId,
                "groundName" to booking.groundName,

                "userId" to booking.userId,

                "date" to booking.date.toString(),
                "startTime" to booking.startTime.toString(),
                "endTime" to booking.endTime.toString()
            )
        ).await()

        dao.insertBooking(booking.copy(bookingId = doc.id))

        return "Booking Confirmed"
    }

    // ===============================
    // 🔥 ONE-TIME SYNC
    // ===============================
    suspend fun syncBookings(date: LocalDate) {

        val snapshot = firestore.collection("bookings")
            .whereEqualTo("date", date.toString())
            .get()
            .await()

        Log.d("SYNC", "Docs: ${snapshot.size()}")

        snapshot.documents.forEach {

            val booking = Booking(
                id = 0,
                bookingId = it.id,

                teamId = it.getString("teamId") ?: "",
                teamName = it.getString("teamName") ?: "",

                sport = it.getString("sport") ?: "",

                groundId = it.getString("groundId") ?: "",
                groundName = it.getString("groundName") ?: "",

                userId = it.getString("userId") ?: "",

                date = LocalDate.parse(it.getString("date")),
                startTime = LocalTime.parse(it.getString("startTime")),
                endTime = LocalTime.parse(it.getString("endTime"))
            )

            dao.insertBooking(booking)
        }
    }

    // ===============================
    // 🔥 REAL-TIME LISTENER (FIXED)
    // ===============================
    fun listenToBookings(
        date: LocalDate,
        groundId: String,
        onUpdate: (List<Booking>) -> Unit
    ): ListenerRegistration {

        return firestore.collection("bookings")
            .whereEqualTo("date", date.toString())
            .whereEqualTo("groundId", groundId)
            .addSnapshotListener { snapshot, error ->

                if (error != null || snapshot == null) return@addSnapshotListener

                val list = snapshot.documents.mapNotNull {

                    try {
                        Booking(
                            id = 0,
                            bookingId = it.id,

                            teamId = it.getString("teamId") ?: "",
                            teamName = it.getString("teamName") ?: "",

                            sport = it.getString("sport") ?: "",

                            groundId = it.getString("groundId") ?: "",
                            groundName = it.getString("groundName") ?: "",

                            userId = it.getString("userId") ?: "",

                            date = LocalDate.parse(it.getString("date")),
                            startTime = LocalTime.parse(it.getString("startTime")),
                            endTime = LocalTime.parse(it.getString("endTime"))
                        )
                    } catch (e: Exception) {
                        null
                    }
                }

                // ✅ FIX: run suspend DAO in coroutine
                CoroutineScope(Dispatchers.IO).launch {
                    list.forEach {
                        dao.insertBooking(it)
                    }
                }

                onUpdate(list)
            }
    }

    // ===============================
    suspend fun getBookings(date: LocalDate): List<Booking> {
        return dao.getBookingsByDate(date)
    }

    fun getBookingsForGround(groundId: String) =
        dao.getBookingsForGround(groundId)
}