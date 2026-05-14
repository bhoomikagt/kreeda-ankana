package com.bhoomika.kreedaankana.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhoomika.kreedaankana.data.local.entity.Booking
import com.bhoomika.kreedaankana.data.model.Challenge
import com.bhoomika.kreedaankana.data.model.ChallengeReply
import com.bhoomika.kreedaankana.data.model.Team
import com.bhoomika.kreedaankana.repository.ChallengeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.bhoomika.kreedaankana.data.model.MatchResult

class ChallengeViewModel : ViewModel() {

    private val repo =
        ChallengeRepository()

    // ==========================
    // OPEN CHALLENGES
    // ==========================
    private val _challenges =
        MutableStateFlow<List<Challenge>>(
            emptyList()
        )

    val challenges:
            StateFlow<List<Challenge>>
            = _challenges

    // ==========================
    // MY CHALLENGES
    // ==========================
    private val _myChallenges =
        MutableStateFlow<List<Challenge>>(
            emptyList()
        )
    private val _acceptedChallenges =
        MutableStateFlow<List<Challenge>>(
            emptyList()
        )

    val acceptedChallenges:
            StateFlow<List<Challenge>>
            = _acceptedChallenges

    val myChallenges:
            StateFlow<List<Challenge>>
            = _myChallenges

    private val _teamAdminState =
        MutableStateFlow<Map<String, Boolean>>(
            emptyMap()
        )

    val teamAdminState:
            StateFlow<Map<String, Boolean>>
            = _teamAdminState

    // ==========================
    // REPLIES
    // ==========================
    private val _replies =
        MutableStateFlow<List<ChallengeReply>>(
            emptyList()
        )

    val replies:
            StateFlow<List<ChallengeReply>>
            = _replies

    // ==========================
    // MESSAGE
    // ==========================
    private val _message =
        MutableStateFlow<String?>(null)

    val message:
            StateFlow<String?>
            = _message

    // ==========================
    // CHALLENGE BOOKING IDS
    // ==========================
    private val _challengeBookingIds =
        MutableStateFlow<Set<String>>(
            emptySet()
        )

    val challengeBookingIds:
            StateFlow<Set<String>>
            = _challengeBookingIds

    fun checkAdmin(
        teamId: String
    ) {

        viewModelScope.launch {

            val result =
                repo.isTeamAdmin(
                    teamId
                )

            _teamAdminState.value =
                _teamAdminState.value
                    .toMutableMap()
                    .apply {

                        put(
                            teamId,
                            result
                        )
                    }
        }
    }

    // ==========================
    // LOAD EVERYTHING
    // ==========================
    fun loadAllChallenges() {

        viewModelScope.launch {

            _challenges.value =
                repo.getOpenChallenges()

            _myChallenges.value =
                repo.getMyChallenges()

            _acceptedChallenges.value =
                repo.getAcceptedChallenges()

            _challengeBookingIds.value =
                repo.getChallengeBookingIds()
        }
    }

    // Backward compatibility
    fun loadChallenges() {
        loadAllChallenges()
    }

    // ==========================
    // CREATE CHALLENGE
    // ==========================
    fun createChallenge(
        booking: Booking,
        description: String
    ) {

        viewModelScope.launch {

            val result =
                repo.createChallenge(
                    booking,
                    description
                )

            _message.value =
                result

            loadAllChallenges()
        }
    }

    // ==========================
    // REPLY TO CHALLENGE
    // ==========================
    fun replyToChallenge(
        challenge: Challenge,
        team: Team,
        message: String
    ) {

        viewModelScope.launch {

            val result =
                repo.replyToChallenge(
                    challenge,
                    team,
                    message
                )

            _message.value =
                result

            loadAllChallenges()
        }
    }

    // ==========================
    // LOAD REPLIES
    // ==========================
    fun loadReplies(
        challengeId: String,
        onLoaded:
            (List<ChallengeReply>) -> Unit
    ) {

        viewModelScope.launch {

            val result =
                repo.getReplies(
                    challengeId
                )

            _replies.value =
                result

            onLoaded(result)
        }
    }

    // ==========================
    // ACCEPT TEAM
    // ==========================
    fun acceptReply(
        challengeId: String,
        reply: ChallengeReply
    ) {

        viewModelScope.launch {

            val result =
                repo.acceptReply(
                    challengeId,
                    reply
                )

            _message.value =
                result

            loadAllChallenges()
        }
    }

    // ==========================
    // CANCEL CHALLENGE
    // ==========================
    fun cancelChallenge(
        challengeId: String
    ) {

        viewModelScope.launch {

            val result =
                repo.cancelChallenge(
                    challengeId
                )

            _message.value =
                result

            loadAllChallenges()
        }
    }

    // ==========================
// POST MATCH RESULT
// ==========================
    fun postMatchResult(
        challenge: Challenge,
        scoreA: Int,
        scoreB: Int,
        summary: String
    ) {

        viewModelScope.launch {

            val result =
                repo.postMatchResult(
                    challenge = challenge,
                    scoreA = scoreA,
                    scoreB = scoreB,
                    summary = summary
                )

            _message.value =
                result

            loadAllChallenges()
        }
    }
    // ==========================
// POST RESULT
// ==========================
    fun postResult(
        challenge: Challenge,
        scoreA: Int,
        scoreB: Int,
        summary: String
    ) {

        viewModelScope.launch {

            val result =
                repo.postResult(
                    challenge =
                        challenge,

                    scoreA =
                        scoreA,

                    scoreB =
                        scoreB,

                    summary =
                        summary
                )

            _message.value =
                result

            loadAllChallenges()
        }
    }
    // ==========================
    // CLEAR MESSAGE
    // ==========================
    fun clearMessage() {
        _message.value = null
    }
}