package com.bhoomika.kreedaankana.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhoomika.kreedaankana.data.model.TeamPreview
import com.bhoomika.kreedaankana.data.model.User
import com.bhoomika.kreedaankana.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val repo = ProfileRepository()

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _teams = MutableStateFlow<List<TeamPreview>>(emptyList())
    val teams: StateFlow<List<TeamPreview>> = _teams

    fun loadProfile() {
        viewModelScope.launch {
            val u = repo.getUser()
            _user.value = u

            if (u != null) {
                _teams.value = repo.getTeamDetails(u.teams)
            }
        }
    }

    fun leaveTeam(teamId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repo.leaveTeam(teamId)
            loadProfile()
            onResult(success)
        }
    }
}