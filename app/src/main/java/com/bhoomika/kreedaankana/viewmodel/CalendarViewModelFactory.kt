package com.bhoomika.kreedaankana.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bhoomika.kreedaankana.repository.BookingRepository

class CalendarViewModelFactory(
    private val repo: BookingRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CalendarViewModel(repo) as T
    }
}