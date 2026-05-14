package com.bhoomika.kreedaankana.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhoomika.kreedaankana.data.model.Member
import com.bhoomika.kreedaankana.data.model.Team
import com.bhoomika.kreedaankana.repository.TeamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TeamViewModel : ViewModel() {

    private val repo = TeamRepository()

    private val _teams = MutableStateFlow<List<Team>>(emptyList())
    val teams: StateFlow<List<Team>> = _teams

    fun loadTeams() {
        viewModelScope.launch {
            try {
                repo.ensureUserExists()
                val result = repo.getUserTeams()

                _teams.value = result

                println("DEBUG: Loaded teams = $result") // 🔥 debug

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun createTeam(
        name: String,
        sport: String,
        members: List<Member>,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val ok = repo.createTeam(name, sport, members)
            if (ok) loadTeams()
            onResult(ok)
        }
    }
}