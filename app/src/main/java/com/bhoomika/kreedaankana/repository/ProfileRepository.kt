package com.bhoomika.kreedaankana.repository

import com.bhoomika.kreedaankana.data.model.TeamPreview
import com.bhoomika.kreedaankana.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ProfileRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    suspend fun getUser(): User? {
        val uid = auth.currentUser?.uid ?: return null

        val doc = db.collection("users").document(uid).get().await()

        return User(
            name = doc.getString("name") ?: "",
            email = doc.getString("email") ?: "",
            teams = doc.get("teams") as? List<String> ?: emptyList()
        )
    }

    fun updateProfile(
        name: String,
        email: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val user = auth.currentUser ?: return
        val uid = user.uid
        val currentEmail = user.email ?: ""

        // ✅ CASE 1: ONLY NAME CHANGE
        if (email.trim() == currentEmail.trim()) {

            db.collection("users")
                .document(uid)
                .update("name", name)
                .addOnSuccessListener { onResult(true, null) }
                .addOnFailureListener { onResult(false, it.message) }

            return
        }

        // ✅ CASE 2: EMAIL CHANGE
        user.updateEmail(email.trim())
            .addOnSuccessListener {

                db.collection("users")
                    .document(uid)
                    .update(
                        mapOf(
                            "name" to name,
                            "email" to email.trim()
                        )
                    )
                    .addOnSuccessListener { onResult(true, null) }
                    .addOnFailureListener { onResult(false, it.message) }

            }
            .addOnFailureListener {
                onResult(false, "Re-login required to change email")
            }
    }

    suspend fun getTeamDetails(teamIds: List<String>): List<TeamPreview> {
        val uid = auth.currentUser?.uid ?: return emptyList()

        val result = mutableListOf<TeamPreview>()

        for (id in teamIds) {

            val doc = db.collection("teams").document(id).get().await()
            val name = doc.getString("teamName") ?: ""

            val members = doc.get("members") as? List<Map<String, Any>> ?: emptyList()

            val isAdmin = members.any {
                it["uid"] == uid && it["role"] == "admin"
            }

            result.add(TeamPreview(id, name, isAdmin))
        }

        return result
    }

    suspend fun leaveTeam(teamId: String): Boolean {
        val uid = auth.currentUser?.uid ?: return false

        val teamDoc = db.collection("teams").document(teamId).get().await()
        val members = teamDoc.get("members") as? List<Map<String, Any>> ?: emptyList()

        val isAdmin = members.any {
            it["uid"] == uid && it["role"] == "admin"
        }

        if (isAdmin) return false // ❌ block admin

        db.collection("users")
            .document(uid)
            .update("teams", FieldValue.arrayRemove(teamId))
            .await()

        db.collection("teams")
            .document(teamId)
            .update(
                "members",
                FieldValue.arrayRemove(mapOf("uid" to uid, "role" to "member"))
            )
            .await()

        return true
    }
}