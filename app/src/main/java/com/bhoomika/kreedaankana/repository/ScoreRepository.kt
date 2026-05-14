package com.bhoomika.kreedaankana.repository

import com.bhoomika.kreedaankana.data.model.Challenge
import com.bhoomika.kreedaankana.data.model.MatchResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ScoreRepository {

    private val db =
        FirebaseFirestore.getInstance()

    private val auth =
        FirebaseAuth.getInstance()

    // ==========================
    // GET ACCEPTED MATCHES
    // ==========================
    suspend fun getUpcomingMatches():
            List<Challenge> {

        return try {

            val resultSnapshot =
                db.collection(
                    "match_results"
                )
                    .get()
                    .await()

            val completedChallengeIds =
                resultSnapshot.documents
                    .mapNotNull {

                        it.getString(
                            "challengeId"
                        )
                    }
                    .toSet()

            val snapshot =
                db.collection(
                    "challenges"
                )
                    .whereEqualTo(
                        "status",
                        "ACCEPTED"
                    )
                    .get()
                    .await()

            val now =
                java.time.LocalDateTime.now()

            snapshot.documents
                .mapNotNull { doc ->

                    val challengeId =
                        doc.id

                    // already posted result
                    if (
                        challengeId
                        in
                        completedChallengeIds
                    ) {
                        return@mapNotNull null
                    }

                    val date =
                        doc.getString(
                            "date"
                        ) ?: return@mapNotNull null

                    val startTime =
                        doc.getString(
                            "startTime"
                        ) ?: return@mapNotNull null

                    val matchDateTime =
                        try {

                            java.time.LocalDateTime.of(
                                java.time.LocalDate.parse(
                                    date
                                ),
                                java.time.LocalTime.parse(
                                    startTime
                                )
                            )

                        } catch (
                            e: Exception
                        ) {

                            return@mapNotNull null
                        }

                    // only future matches
                    if (
                        matchDateTime.isBefore(
                            now
                        )
                    ) {
                        return@mapNotNull null
                    }

                    Challenge(

                        id =
                            challengeId,

                        bookingId =
                            doc.getString(
                                "bookingId"
                            ) ?: "",

                        hostUserId =
                            doc.getString(
                                "hostUserId"
                            ) ?: "",

                        hostTeamId =
                            doc.getString(
                                "hostTeamId"
                            ) ?: "",

                        hostTeamName =
                            doc.getString(
                                "hostTeamName"
                            ) ?: "",

                        sport =
                            doc.getString(
                                "sport"
                            ) ?: "",

                        groundId =
                            doc.getString(
                                "groundId"
                            ) ?: "",

                        groundName =
                            doc.getString(
                                "groundName"
                            ) ?: "",

                        date = date,

                        startTime =
                            startTime,

                        endTime =
                            doc.getString(
                                "endTime"
                            ) ?: "",

                        description =
                            doc.getString(
                                "description"
                            ) ?: "",

                        status =
                            doc.getString(
                                "status"
                            ) ?: "",

                        acceptedTeamId =
                            doc.getString(
                                "acceptedTeamId"
                            ),

                        acceptedTeamName =
                            doc.getString(
                                "acceptedTeamName"
                            )
                    )
                }
                .sortedBy {

                    java.time.LocalDateTime.of(
                        java.time.LocalDate.parse(
                            it.date
                        ),
                        java.time.LocalTime.parse(
                            it.startTime
                        )
                    )
                }

        } catch (
            e: Exception
        ) {

            e.printStackTrace()
            emptyList()
        }
    }

    // ==========================
    // POST SCORE
    // ==========================
    // ==========================
// POST SCORE
// ==========================
    suspend fun postScore(
        challenge: Challenge,
        hostScore: Int,
        opponentScore: Int
    ): String {

        return try {

            val uid =
                auth.currentUser?.uid
                    ?: return "User not logged in"

            val winnerId =
                if (
                    hostScore >
                    opponentScore
                ) {
                    challenge.hostTeamId
                } else {
                    challenge.acceptedTeamId
                        ?: ""
                }

            val winnerName =
                if (
                    hostScore >
                    opponentScore
                ) {
                    challenge.hostTeamName
                } else {
                    challenge.acceptedTeamName
                        ?: ""
                }

            val result =
                MatchResult(

                    challengeId =
                        challenge.id,

                    hostTeamId =
                        challenge.hostTeamId,

                    hostTeamName =
                        challenge.hostTeamName,

                    opponentTeamId =
                        challenge.acceptedTeamId
                            ?: "",

                    opponentTeamName =
                        challenge.acceptedTeamName
                            ?: "",

                    sport =
                        challenge.sport,

                    groundName =
                        challenge.groundName,

                    date =
                        challenge.date,

                    hostScore =
                        hostScore,

                    opponentScore =
                        opponentScore,

                    winnerTeamId =
                        winnerId,

                    winnerTeamName =
                        winnerName,

                    postedBy =
                        uid
                )

            // Save result
            db.collection(
                "match_results"
            )
                .add(result)
                .await()

            // UPDATE CHALLENGE STATUS
            db.collection(
                "challenges"
            )
                .document(
                    challenge.id
                )
                .update(
                    mapOf(
                        "status" to
                                "COMPLETED",

                        "resultPosted" to
                                true
                    )
                )
                .await()

            println(
                "STATUS UPDATED"
            )

            "Score Posted"

        } catch (e: Exception) {

            e.printStackTrace()

            println(
                "FIREBASE ERROR"
            )

            println(
                e.message
            )

            e.message ?: "Failed"
        }
    }

    // ==========================
    // SCORE WALL
    // ==========================
    suspend fun getResults():
            List<MatchResult> {

        return try {

            db.collection(
                "match_results"
            )
                .get()
                .await()
                .documents
                .map {

                    MatchResult(

                        id =
                            it.id,

                        challengeId =
                            it.getString(
                                "challengeId"
                            ) ?: "",

                        hostTeamId =
                            it.getString(
                                "hostTeamId"
                            ) ?: "",

                        hostTeamName =
                            it.getString(
                                "hostTeamName"
                            ) ?: "",

                        opponentTeamId =
                            it.getString(
                                "opponentTeamId"
                            ) ?: "",

                        opponentTeamName =
                            it.getString(
                                "opponentTeamName"
                            ) ?: "",

                        sport =
                            it.getString(
                                "sport"
                            ) ?: "",

                        groundName =
                            it.getString(
                                "groundName"
                            ) ?: "",

                        date =
                            it.getString(
                                "date"
                            ) ?: "",

                        hostScore =
                            (
                                    it.getLong(
                                        "hostScore"
                                    ) ?: 0
                                    ).toInt(),

                        opponentScore =
                            (
                                    it.getLong(
                                        "opponentScore"
                                    ) ?: 0
                                    ).toInt(),

                        winnerTeamId =
                            it.getString(
                                "winnerTeamId"
                            ) ?: "",

                        winnerTeamName =
                            it.getString(
                                "winnerTeamName"
                            ) ?: "",

                        postedBy =
                            it.getString(
                                "postedBy"
                            ) ?: "",

                        createdAt =
                            it.getLong(
                                "createdAt"
                            ) ?: 0L
                    )
                }

        } catch (
            e: Exception
        ) {

            e.printStackTrace()
            emptyList()
        }
    }
}