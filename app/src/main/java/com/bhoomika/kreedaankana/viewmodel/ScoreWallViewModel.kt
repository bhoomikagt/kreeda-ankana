package com.bhoomika.kreedaankana.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhoomika.kreedaankana.data.model.Challenge
import com.bhoomika.kreedaankana.data.model.MatchResult
import com.bhoomika.kreedaankana.repository.ScoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ScoreWallViewModel : ViewModel() {

    private val repo =
        ScoreRepository()

    // ==========================
    // UPCOMING MATCHES
    // ==========================
    private val _upcomingMatches =
        MutableStateFlow<List<Challenge>>(
            emptyList()
        )

    val upcomingMatches:
            StateFlow<List<Challenge>>
            = _upcomingMatches

    // ==========================
    // MATCH RESULTS
    // ==========================
    private val _results =
        MutableStateFlow<List<MatchResult>>(
            emptyList()
        )

    val results:
            StateFlow<List<MatchResult>>
            = _results

    // ==========================
    // MESSAGE
    // ==========================
    private val _message =
        MutableStateFlow<String?>(
            null
        )

    val message:
            StateFlow<String?>
            = _message

    // ==========================
    // LOAD DATA
    // ==========================
    fun loadData() {

        viewModelScope.launch {

            _upcomingMatches.value =
                repo.getUpcomingMatches()

            _results.value =
                repo.getResults()
                    .sortedByDescending {
                        it.createdAt
                    }
        }
    }

    // ==========================
    // POST SCORE
    // ==========================
    fun postScore(
        challenge: Challenge,
        hostScore: Int,
        opponentScore: Int
    ) {

        println("VM CALLED")

        viewModelScope.launch {

            val result =
                repo.postScore(
                    challenge,
                    hostScore,
                    opponentScore
                )

            println(result)

            _message.value =
                result

            loadData()
        }
    }

    // ==========================
    // CLEAR MESSAGE
    // ==========================
    fun clearMessage() {

        _message.value =
            null
    }
}