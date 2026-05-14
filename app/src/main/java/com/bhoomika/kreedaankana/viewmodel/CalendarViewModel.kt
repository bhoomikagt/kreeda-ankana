package com.bhoomika.kreedaankana.viewmodel

import androidx.lifecycle.ViewModel
import com.bhoomika.kreedaankana.data.local.entity.Booking
import com.bhoomika.kreedaankana.repository.BookingRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate

class CalendarViewModel(
    private val repository: BookingRepository
) : ViewModel() {

    // ===============================
    // 🔥 BOOKINGS STATE
    // ===============================
    private val _bookings = MutableStateFlow<List<Booking>>(emptyList())
    val bookings: StateFlow<List<Booking>> = _bookings

    // ===============================
    // 🔥 LOADING STATE (IMPORTANT FIX)
    // ===============================
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // ===============================
    // 🔥 CURRENT GROUND
    // ===============================
    private val _currentGroundId = MutableStateFlow<String?>(null)
    val currentGroundId: String?
        get() = _currentGroundId.value

    private var listener: ListenerRegistration? = null

    // ===============================
    // 🔥 SET GROUND
    // ===============================
    fun setGround(groundId: String) {
        _currentGroundId.value = groundId
    }

    // ===============================
    // 🔥 REAL-TIME LISTENER (FINAL)
    // ===============================
    fun listenBookings(date: LocalDate, groundId: String) {

        // 🔴 remove old listener (prevents memory leak)
        listener?.remove()

        _isLoading.value = true

        listener = repository.listenToBookings(
            date = date,
            groundId = groundId
        ) { data ->

            _bookings.value = data
            _isLoading.value = false
        }
    }

    // ===============================
    // 🔥 CLEANUP
    // ===============================
    override fun onCleared() {
        super.onCleared()
        listener?.remove()
    }
}