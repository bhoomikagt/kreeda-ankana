package com.bhoomika.kreedaankana.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun signUp(
        name: String,
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {

        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { authTask ->

                if (authTask.isSuccessful) {

                    val uid = auth.currentUser?.uid
                    if (uid == null) {
                        onResult(false, "User ID not found")
                        return@addOnCompleteListener
                    }

                    val userRef = db.collection("users").document(uid)

                    // 🔥 STEP 1: FETCH INVITES FOR THIS EMAIL
                    db.collection("invites")
                        .whereEqualTo("email", email)
                        .whereEqualTo("status", "pending")
                        .get()
                        .addOnCompleteListener { inviteTask ->

                            if (inviteTask.isSuccessful) {

                                val invites = inviteTask.result?.documents ?: emptyList()

                                val teamIds = invites.mapNotNull {
                                    it.getString("teamId")
                                }

                                // 🔥 STEP 2: CREATE USER WITH TEAMS FROM INVITES
                                val user = hashMapOf(
                                    "name" to name,
                                    "email" to email,
                                    "teams" to teamIds
                                )

                                userRef.set(user)
                                    .addOnCompleteListener { userTask ->

                                        if (userTask.isSuccessful) {

                                            // 🔥 STEP 3: MARK INVITES AS ACCEPTED
                                            invites.forEach { invite ->
                                                db.collection("invites")
                                                    .document(invite.id)
                                                    .update("status", "accepted")
                                            }

                                            onResult(true, null)

                                        } else {
                                            onResult(false, userTask.exception?.message)
                                        }
                                    }

                            } else {
                                onResult(false, inviteTask.exception?.message)
                            }
                        }

                } else {
                    onResult(false, authTask.exception?.message)
                }
            }
    }

    fun login(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { onResult(false, it.message) }
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }
}