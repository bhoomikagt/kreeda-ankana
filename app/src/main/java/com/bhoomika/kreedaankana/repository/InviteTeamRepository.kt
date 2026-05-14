package com.bhoomika.kreedaankana.repository

import com.bhoomika.kreedaankana.data.model.Invite
import com.bhoomika.kreedaankana.data.model.Reply
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class InviteTeamRepository {

    private val db = FirebaseFirestore.getInstance()

    // ===============================
    // 🔥 CREATE invite
    // ===============================
    suspend fun createinvite(
        hostTeamId: String,
        hostTeamName: String,

        opponentTeamId: String,
        opponentTeamName: String,

        sport: String,

        date: String,
        startTime: String,
        endTime: String,

        message: String,
        uid: String
    ) {

        db.collection("teamInvites").add(
            mapOf(
                "hostTeamId" to hostTeamId,
                "hostTeamName" to hostTeamName,

                "opponentTeamId" to opponentTeamId,
                "opponentTeamName" to opponentTeamName,

                "sport" to sport,

                "date" to date,
                "startTime" to startTime,
                "endTime" to endTime,

                "message" to message,

                "createdBy" to uid,
                "createdAt" to FieldValue.serverTimestamp(),
                "status" to "open"
            )
        ).await()
    }

    // ===============================
    // 🔥 GET inviteS
    // ===============================
    suspend fun getinvites(): List<Invite> {

        val snapshot = db.collection("teamInvites")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()

        return snapshot.documents.map {
            Invite(
                id = it.id,

                hostTeamId = it.getString("hostTeamId") ?: "",
                hostTeamName = it.getString("hostTeamName") ?: "",

                opponentTeamId = it.getString("opponentTeamId") ?: "",
                opponentTeamName = it.getString("opponentTeamName") ?: "",

                sport = it.getString("sport") ?: "",

                date = it.getString("date") ?: "",
                startTime = it.getString("startTime") ?: "",
                endTime = it.getString("endTime") ?: "",

                message = it.getString("message") ?: "",
                status = it.getString("status") ?: "open"
            )
        }
    }

    // ===============================
    // 🔥 ADD REPLY
    // ===============================
    suspend fun addReply(
        inviteId: String,
        teamId: String,
        teamName: String,
        message: String,
        uid: String,
        userName: String
    ) {

        val user = FirebaseAuth.getInstance().currentUser

        val data = hashMapOf<String, Any>(
            "teamId" to teamId,
            "teamName" to teamName,
            "message" to message,
            "createdBy" to uid,
            "createdByName" to userName, // ✅ FIXED
            "createdAt" to FieldValue.serverTimestamp()
        )

        db.collection("teamInvites")
            .document(inviteId)
            .collection("replies")
            .add(data)
            .await()
    }

    // ===============================
    // 🔥 GET REPLIES (FIXED ORDER)
    // ===============================
    suspend fun getReplies(inviteId: String): List<Reply> {

        val snapshot = db.collection("teamInvites")
            .document(inviteId)
            .collection("replies")
            .orderBy("createdAt", Query.Direction.ASCENDING) // ✅ FIX
            .get()
            .await()

        return snapshot.documents.map {
            Reply(
                id = it.id,
                teamId = it.getString("teamId") ?: "",
                teamName = it.getString("teamName") ?: "",
                userName = it.getString("createdByName") ?: "User",
                message = it.getString("message") ?: ""
            )
        }
    }

    // ===============================
    // 🔥 ACCEPT invite
    // ===============================
    suspend fun acceptinvite(inviteId: String) {
        db.collection("teamInvites")
            .document(inviteId)
            .update("status", "accepted")
            .await()
    }
}