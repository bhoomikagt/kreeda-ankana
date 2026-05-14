package com.bhoomika.kreedaankana.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhoomika.kreedaankana.data.model.Ground
import com.bhoomika.kreedaankana.repository.GroundRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GroundViewModel : ViewModel() {

    private val repo = GroundRepository()

    private val _grounds = MutableStateFlow<List<Ground>>(emptyList())
    val grounds: StateFlow<List<Ground>> = _grounds

    fun loadGrounds() {
        viewModelScope.launch {
            _grounds.value = repo.getGrounds()
        }
    }
}