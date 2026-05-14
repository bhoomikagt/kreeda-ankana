package com.bhoomika.kreedaankana.repository

import com.bhoomika.kreedaankana.data.model.Ground
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class GroundRepository {

    private val db = FirebaseFirestore.getInstance()

    suspend fun getGrounds(): List<Ground> {

        val snapshot = db.collection("grounds").get().await()

        return snapshot.documents.map {
            Ground(
                id = it.id,
                name = it.getString("name") ?: "",
                sports = it.get("sports") as? List<String> ?: emptyList()
            )
        }
    }
}