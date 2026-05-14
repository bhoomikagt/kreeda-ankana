package com.bhoomika.kreedaankana.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhoomika.kreedaankana.data.local.entity.Booking
import com.bhoomika.kreedaankana.repository.BookingRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

class BookingViewModel(
    private val repository: BookingRepository
) : ViewModel() {

    private val _bookings = MutableStateFlow<List<Booking>>(emptyList())
    val bookings: StateFlow<List<Booking>> = _bookings

    private val _bookingMessage = MutableStateFlow<String?>(null)
    val bookingMessage: StateFlow<String?> = _bookingMessage

    // 🔥 listener reference (important)
    private var bookingListener: ListenerRegistration? = null

    // ===============================
    // 🔥 BOOK SLOT
    // ===============================
    fun bookSlot(booking: Booking) {
        viewModelScope.launch {

            val uid = FirebaseAuth.getInstance().currentUser?.uid
                ?: return@launch

            val message = repository.bookSlot(
                booking.copy(userId = uid)
            )

            _bookingMessage.value = message
        }
    }

    // ===============================
    // 🔥 ONE-TIME LOAD (fallback)
    // ===============================
    fun loadBookings(date: LocalDate) {
        viewModelScope.launch {

            repository.syncBookings(date)

            val data = repository.getBookings(date)

            _bookings.value = (_bookings.value + data)
                .distinctBy { it.bookingId }
        }
    }

    // ===============================
    // 🔥 REAL-TIME LISTENER (FIXED)
    // ===============================
    fun listenBookings(date: LocalDate, groundId: String) {

        // 🔴 remove old listener (VERY IMPORTANT)
        bookingListener?.remove()

        bookingListener = repository.listenToBookings(
            date = date,
            groundId = groundId
        ) { data ->

            _bookings.value = data
        }
    }

    // ===============================
    fun clearMessage() {
        _bookingMessage.value = null
    }

    fun clearBookings() {
        _bookings.value = emptyList()
    }

    // ===============================
    // 🔥 CLEANUP
    // ===============================
    override fun onCleared() {
        super.onCleared()
        bookingListener?.remove()
    }
}