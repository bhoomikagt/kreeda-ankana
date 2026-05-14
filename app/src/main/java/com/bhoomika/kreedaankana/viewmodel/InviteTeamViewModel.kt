package com.bhoomika.kreedaankana.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhoomika.kreedaankana.data.local.entity.Booking
import com.bhoomika.kreedaankana.data.model.Invite
import com.bhoomika.kreedaankana.data.model.Team
import com.bhoomika.kreedaankana.data.model.Reply
import com.bhoomika.kreedaankana.repository.InviteTeamRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class InviteTeamViewModel : ViewModel() {

    private val repo = InviteTeamRepository()

    // 🔥 SINGLE invite REPLIES (used in ReplyScreen)
    private val _replies = MutableStateFlow<List<Reply>>(emptyList())
    val replies: StateFlow<List<Reply>> = _replies

    // 🔥 MULTIPLE invite REPLIES (used in inviteScreen)
    private val _replyMap = MutableStateFlow<Map<String, List<Reply>>>(emptyMap())
    val replyMap: StateFlow<Map<String, List<Reply>>> = _replyMap

    // 🔥 inviteS
    private val _invites = MutableStateFlow<List<Invite>>(emptyList())
    val invites: StateFlow<List<Invite>> = _invites

    // 🔥 USER TEAMS
    private val _userTeamIds = MutableStateFlow<List<String>>(emptyList())
    val userTeamIds: StateFlow<List<String>> = _userTeamIds

    // ===============================
    // 🔥 LOAD inviteS
    // ===============================
    fun loadinvites() {
        viewModelScope.launch {
            _invites.value = repo.getinvites()
        }
    }

    // ===============================
    // 🔥 CREATE invite
    // ===============================
    fun createInvite(
        hostTeam: Team,
        opponentTeam: Team,
        booking: Booking,
        message: String
    ) {
        viewModelScope.launch {
            try {
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                    ?: return@launch

                repo.createinvite(
                    hostTeamId = hostTeam.id,
                    hostTeamName = hostTeam.teamName,
                    opponentTeamId = opponentTeam.id,
                    opponentTeamName = opponentTeam.teamName,
                    sport = hostTeam.teamSport,
                    date = booking.date.toString(),
                    startTime = booking.startTime.toString(),
                    endTime = booking.endTime.toString(),
                    message = message,
                    uid = uid
                )

                println("invite created successfully")

                loadinvites()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ===============================
    // 🔥 LOAD USER TEAMS
    // ===============================
    fun loadUserTeams() {
        viewModelScope.launch {

            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch

            val doc = FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .await()

            _userTeamIds.value =
                (doc.get("teams") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
        }
    }

    // ===============================
    // 🔥 LOAD REPLIES (SINGLE SCREEN)
    // ===============================
    fun loadReplies(inviteId: String) {
        viewModelScope.launch {
            _replies.value = repo.getReplies(inviteId)
        }
    }

    // ===============================
    // 🔥 LOAD REPLIES PER invite (FOR LIST UI)
    // ===============================
    fun loadRepliesForinvite(inviteId: String) {
        viewModelScope.launch {

            val data = repo.getReplies(inviteId)

            _replyMap.value = _replyMap.value.toMutableMap().apply {
                put(inviteId, data)
            }
        }
    }

    // ===============================
    // 🔥 AUTO REPLY (NO TEAM SELECT)
    // ===============================
    fun addReplyAuto(
        inviteId: String,
        message: String
    ) {
        viewModelScope.launch {

            val uid = FirebaseAuth.getInstance().currentUser?.uid
                ?: return@launch

            if (message.isBlank()) return@launch

            val invite = invites.value.find { it.id == inviteId }
                ?: return@launch

            // 🔥 FETCH USER NAME FROM FIRESTORE
            val userDoc = FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .await()

            val userName = userDoc.getString("name") ?: "Player"

            // 🔥 SEND TO REPO
            repo.addReply(
                inviteId = inviteId,
                teamId = invite.opponentTeamId,
                teamName = invite.opponentTeamName,
                message = message.trim(),
                uid = uid,
                userName = userName   // ✅ NEW
            )

            loadReplies(inviteId)
            loadRepliesForinvite(inviteId)
        }
    }

    // ===============================
    // 🔥 REPLY + ACCEPT
    // ===============================
    fun replyAndAccept(
        inviteId: String,
        message: String
    ) {
        viewModelScope.launch {

            val uid = FirebaseAuth.getInstance().currentUser!!.uid

            val invite = invites.value.find { it.id == inviteId }
                ?: return@launch

            val userDoc = FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .await()

            val userName = userDoc.getString("name") ?: "Player"

            // reply
            repo.addReply(
                inviteId,
                invite.opponentTeamId,
                invite.opponentTeamName,
                message,
                uid,
                userName
            )

            // accept
            repo.acceptinvite(inviteId)

            // refresh everything
            loadReplies(inviteId)
            loadRepliesForinvite(inviteId)
            loadinvites()
        }
    }
    fun addReplyAsHost(
        inviteId: String,
        message: String
    ) {
        viewModelScope.launch {

            val uid = FirebaseAuth.getInstance().currentUser?.uid
                ?: return@launch

            if (message.isBlank()) return@launch

            val invite = invites.value.find { it.id == inviteId }
                ?: return@launch

            // 🔥 FETCH USER NAME FROM USERS COLLECTION
            val userDoc = FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .await()

            val userName = userDoc.getString("name") ?: "Player"

            // 🔥 SAVE AS HOST TEAM (IMPORTANT FIX)
            repo.addReply(
                inviteId = inviteId,
                teamId = invite.hostTeamId,        // ✅ HOST TEAM
                teamName = invite.hostTeamName,    // ✅ HOST TEAM
                message = message.trim(),
                uid = uid,
                userName = userName
            )

            // 🔄 REFRESH UI
            loadReplies(inviteId)
            loadRepliesForinvite(inviteId)
        }
    }
}