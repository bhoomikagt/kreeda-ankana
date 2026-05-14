package com.bhoomika.kreedaankana.repository

import com.bhoomika.kreedaankana.data.model.Member
import com.bhoomika.kreedaankana.data.model.Team
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class TeamRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun ensureUserExists() {
        val uid = auth.currentUser?.uid ?: return
        val ref = db.collection("users").document(uid)
        val snap = ref.get().await()
        if (!snap.exists()) {
            ref.set(mapOf("name" to "User", "teams" to emptyList<String>())).await()
        }
    }

    // 🔥 Create team + map to user
    suspend fun createTeam(
        teamName: String,
        sport: String,
        members: List<Member>
    ): Boolean {

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return false
        val db = FirebaseFirestore.getInstance()

        return try {

            val cleanMembers = members
                .map { it.email.trim().lowercase() }
                .filter { it.isNotBlank() }
                .distinct()

            // 🔥 1. CREATE TEAM WITH CREATOR AS ADMIN
            val team = hashMapOf(
                "teamName" to teamName,
                "teamSport" to sport,
                "createdBy" to uid,
                "members" to listOf(
                    mapOf("uid" to uid, "role" to "admin")
                ),
                "createdAt" to FieldValue.serverTimestamp()
            )

            val teamDoc = db.collection("teams").add(team).await()
            val teamId = teamDoc.id

            // 🔥 2. ADD TEAM TO CREATOR
            db.collection("users")
                .document(uid)
                .update("teams", FieldValue.arrayUnion(teamId))
                .await()

            // 🔥 3. HANDLE MEMBERS
            for (email in cleanMembers) {

                // ❌ Skip self
                val currentUserEmail =
                    FirebaseAuth.getInstance().currentUser?.email?.lowercase()

                if (email == currentUserEmail) continue

                val snapshot = db.collection("users")
                    .whereEqualTo("email", email)
                    .get()
                    .await()

                if (!snapshot.isEmpty) {

                    // ✅ EXISTING USER
                    val userDoc = snapshot.documents.first()
                    val userRef = userDoc.reference
                    val memberUid = userDoc.id

                    // ➤ Add team to user
                    userRef.update(
                        "teams",
                        FieldValue.arrayUnion(teamId)
                    ).await()

                    // ➤ Add user to team members
                    db.collection("teams")
                        .document(teamId)
                        .update(
                            "members",
                            FieldValue.arrayUnion(
                                mapOf(
                                    "uid" to memberUid,
                                    "role" to "member"
                                )
                            )
                        )
                        .await()

                } else {

                    // ❌ USER NOT FOUND → CREATE INVITE (NO DUPLICATE)

                    val existingInvite = db.collection("invites")
                        .whereEqualTo("email", email)
                        .whereEqualTo("teamId", teamId)
                        .whereEqualTo("status", "pending")
                        .get()
                        .await()

                    if (existingInvite.isEmpty) {

                        val invite = hashMapOf(
                            "teamId" to teamId,
                            "teamName" to teamName,
                            "email" to email,
                            "status" to "pending",
                            "createdAt" to FieldValue.serverTimestamp()
                        )

                        db.collection("invites").add(invite).await()
                    }
                }
            }

            true

        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    // 🔥 Fetch only user's teams (by ids)
    suspend fun getUserTeams(): List<Team> {
        val uid = auth.currentUser?.uid ?: return emptyList()

        val userDoc = db.collection("users").document(uid).get().await()
        val ids = (userDoc.get("teams") as? List<*>)?.filterIsInstance<String>() ?: emptyList()

        if (ids.isEmpty()) return emptyList()

        val snap = db.collection("teams")
            .whereIn(FieldPath.documentId(), ids)
            .get()
            .await()

        return snap.documents.map {
            Team(
                id = it.id,
                teamName = it.getString("teamName") ?: "",
                teamSport = it.getString("teamSport") ?: ""
            )
        }
    }
}