package com.bhoomika.kreedaankana.repository

import com.bhoomika.kreedaankana.data.local.entity.Booking
import com.bhoomika.kreedaankana.data.model.Challenge
import com.bhoomika.kreedaankana.data.model.ChallengeReply
import com.bhoomika.kreedaankana.data.model.Team
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.bhoomika.kreedaankana.data.model.MatchResult
class ChallengeRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // =====================================
    // CREATE CHALLENGE
    // =====================================
    suspend fun createChallenge(
        booking: Booking,
        description: String
    ): String {

        return try {

            val uid =
                auth.currentUser?.uid
                    ?: return "User not logged in"

            val existingChallenge =
                db.collection("challenges")
                    .whereEqualTo(
                        "bookingId",
                        booking.bookingId
                    )
                    .get()
                    .await()

            if (!existingChallenge.isEmpty) {
                return "Challenge already exists"
            }

            val challenge = Challenge(

                bookingId = booking.bookingId,

                hostUserId = uid,

                hostTeamId = booking.teamId,
                hostTeamName = booking.teamName,

                sport = booking.sport,

                groundId = booking.groundId,
                groundName = booking.groundName,

                date = booking.date.toString(),
                startTime = booking.startTime.toString(),
                endTime = booking.endTime.toString(),

                description = description,

                status = "OPEN"
            )

            db.collection("challenges")
                .add(challenge)
                .await()

            "Challenge Posted"

        } catch (e: Exception) {

            e.printStackTrace()
            e.message ?: "Something went wrong"
        }
    }

    // =====================================
    // OPEN CHALLENGES
    // (EXCLUDE MY OWN)
    // =====================================
    suspend fun getOpenChallenges():
            List<Challenge> {

        return try {

            val uid =
                auth.currentUser?.uid
                    ?: return emptyList()

            val snapshot =
                db.collection("challenges")
                    .whereEqualTo(
                        "status",
                        "OPEN"
                    )
                    .get()
                    .await()

            snapshot.documents
                .filter {

                    // REMOVE MY CHALLENGES
                    it.getString(
                        "hostUserId"
                    ) != uid
                }
                .map {
                    mapChallenge(it)
                }

        } catch (e: Exception) {

            e.printStackTrace()
            emptyList()
        }
    }

    // =====================================
    // MY CHALLENGES
    // =====================================
    suspend fun getMyChallenges():
            List<Challenge> {

        return try {

            val uid =
                auth.currentUser?.uid
                    ?: return emptyList()

            val snapshot =
                db.collection("challenges")
                    .whereEqualTo(
                        "hostUserId",
                        uid
                    )
                    .get()
                    .await()

            snapshot.documents.map {
                mapChallenge(it)
            }

        } catch (e: Exception) {

            e.printStackTrace()
            emptyList()
        }
    }

    // =====================================
    // REPLY TO CHALLENGE
    // =====================================
    suspend fun replyToChallenge(
        challenge: Challenge,
        team: Team,
        message: String
    ): String {

        return try {

            if (
                challenge.hostTeamId
                == team.id
            ) {
                return "You cannot reply to your own challenge"
            }

            val replyRef =
                db.collection("challenges")
                    .document(
                        challenge.id
                    )
                    .collection(
                        "replies"
                    )

            val existingReply =
                replyRef
                    .whereEqualTo(
                        "teamId",
                        team.id
                    )
                    .get()
                    .await()

            if (!existingReply.isEmpty) {
                return "Already replied"
            }

            if (
                challenge.sport.lowercase()
                !=
                team.teamSport.lowercase()
            ) {
                return "Sport mismatch"
            }

            val reply =
                ChallengeReply(
                    challengeId =
                        challenge.id,

                    teamId =
                        team.id,

                    teamName =
                        team.teamName,

                    message =
                        message
                )

            replyRef
                .add(reply)
                .await()

            "Reply Sent"

        } catch (e: Exception) {

            e.printStackTrace()
            e.message ?: "Failed"
        }
    }

    // =====================================
    // GET REPLIES
    // =====================================
    suspend fun getReplies(
        challengeId: String
    ): List<ChallengeReply> {

        return try {

            val snapshot =
                db.collection("challenges")
                    .document(
                        challengeId
                    )
                    .collection(
                        "replies"
                    )
                    .get()
                    .await()

            snapshot.documents.map {

                ChallengeReply(
                    id = it.id,

                    challengeId =
                        it.getString(
                            "challengeId"
                        ) ?: "",

                    teamId =
                        it.getString(
                            "teamId"
                        ) ?: "",

                    teamName =
                        it.getString(
                            "teamName"
                        ) ?: "",

                    message =
                        it.getString(
                            "message"
                        ) ?: "",

                    status =
                        it.getString(
                            "status"
                        ) ?: "PENDING",

                    createdAt =
                        it.getLong(
                            "createdAt"
                        ) ?: 0L
                )
            }

        } catch (e: Exception) {

            e.printStackTrace()
            emptyList()
        }
    }

    // =====================================
    // ACCEPT REPLY
    // =====================================
    suspend fun acceptReply(
        challengeId: String,
        reply: ChallengeReply
    ): String {

        return try {

            val challengeRef =
                db.collection("challenges")
                    .document(challengeId)

            // ==========================
            // UPDATE CHALLENGE
            // ==========================
            challengeRef.update(
                mapOf(
                    "status" to "ACCEPTED",

                    "acceptedTeamId"
                            to reply.teamId,

                    "acceptedTeamName"
                            to reply.teamName
                )
            ).await()

            // ==========================
            // UPDATE REPLIES STATUS
            // ==========================
            val repliesSnapshot =
                challengeRef
                    .collection("replies")
                    .get()
                    .await()

            repliesSnapshot.documents.forEach {

                val status =
                    if (it.id == reply.id)
                        "ACCEPTED"
                    else
                        "REJECTED"

                it.reference.update(
                    "status",
                    status
                ).await()
            }

            "Challenge Accepted"

        } catch (e: Exception) {

            e.printStackTrace()
            e.message ?: "Failed"
        }
    }

    // =====================================
    // CANCEL
    // =====================================
    suspend fun cancelChallenge(
        challengeId: String
    ): String {

        return try {

            db.collection("challenges")
                .document(
                    challengeId
                )
                .update(
                    "status",
                    "CANCELLED"
                )
                .await()

            "Challenge Cancelled"

        } catch (e: Exception) {

            e.printStackTrace()
            e.message ?: "Failed"
        }
    }

    // =====================================
    // COMMON MAPPER
    // =====================================
    private fun mapChallenge(
        it: DocumentSnapshot
    ): Challenge {

        return Challenge(

            id = it.id,

            bookingId =
                it.getString(
                    "bookingId"
                ) ?: "",

            hostUserId =
                it.getString(
                    "hostUserId"
                ) ?: "",

            hostTeamId =
                it.getString(
                    "hostTeamId"
                ) ?: "",

            hostTeamName =
                it.getString(
                    "hostTeamName"
                ) ?: "",

            sport =
                it.getString(
                    "sport"
                ) ?: "",

            groundId =
                it.getString(
                    "groundId"
                ) ?: "",

            groundName =
                it.getString(
                    "groundName"
                ) ?: "",

            date =
                it.getString(
                    "date"
                ) ?: "",

            startTime =
                it.getString(
                    "startTime"
                ) ?: "",

            endTime =
                it.getString(
                    "endTime"
                ) ?: "",

            description =
                it.getString(
                    "description"
                ) ?: "",

            status =
                it.getString(
                    "status"
                ) ?: "OPEN",

            acceptedTeamId =
                it.getString(
                    "acceptedTeamId"
                ),

            acceptedTeamName =
                it.getString(
                    "acceptedTeamName"
                ),

            createdAt =
                it.getLong(
                    "createdAt"
                ) ?: 0L
        )
    }

    suspend fun getChallengeBookingIds():
            Set<String> {

        return try {

            val uid =
                auth.currentUser?.uid
                    ?: return emptySet()

            val snapshot =
                db.collection("challenges")
                    .whereEqualTo(
                        "hostUserId",
                        uid
                    )
                    .get()
                    .await()

            snapshot.documents.mapNotNull {

                it.getString(
                    "bookingId"
                )

            }.toSet()

        } catch (e: Exception) {

            e.printStackTrace()
            emptySet()
        }
    }

    // =====================================
// ACCEPTED CHALLENGES
// (MY TEAM GOT ACCEPTED)
// =====================================
    // =====================================
// ACCEPTED CHALLENGES
// (MY TEAM GOT ACCEPTED)
// =====================================
    // =====================================
// ACCEPTED CHALLENGES
// (MY TEAM GOT ACCEPTED)
// =====================================
    suspend fun getAcceptedChallenges():
            List<Challenge> {

        return try {

            val uid =
                auth.currentUser?.uid
                    ?: return emptyList()

            // Get all teams
            val teamsSnapshot =
                db.collection("teams")
                    .get()
                    .await()

            // Find teams where current user is a member
            val myTeamIds =
                teamsSnapshot.documents
                    .filter { document ->

                        val members =
                            document.get(
                                "members"
                            ) as? List<Map<String, Any>>
                                ?: emptyList()

                        members.any { member ->

                            member["uid"] == uid
                        }
                    }
                    .map {
                        it.id
                    }

            if (myTeamIds.isEmpty()) {
                return emptyList()
            }

            // Get accepted challenges
            val challengesSnapshot =
                db.collection("challenges")
                    .whereEqualTo(
                        "status",
                        "ACCEPTED"
                    )
                    .get()
                    .await()

            challengesSnapshot.documents
                .filter { document ->

                    val acceptedTeamId =
                        document.getString(
                            "acceptedTeamId"
                        )

                    acceptedTeamId in myTeamIds
                }
                .map {
                    mapChallenge(it)
                }

        } catch (e: Exception) {

            e.printStackTrace()
            emptyList()
        }
    }

    // =====================================
// CHECK TEAM ADMIN
// =====================================
    suspend fun isTeamAdmin(
        teamId: String
    ): Boolean {

        return try {

            val uid =
                auth.currentUser?.uid
                    ?: return false

            val document =
                db.collection("teams")
                    .document(teamId)
                    .get()
                    .await()

            val members =
                document.get(
                    "members"
                ) as? List<Map<String, Any>>
                    ?: return false

            members.any { member ->

                member["uid"] == uid &&
                        member["role"] == "admin"
            }

        } catch (e: Exception) {

            e.printStackTrace()
            false
        }
    }

    // =====================================
// POST MATCH RESULT
// =====================================
    suspend fun postMatchResult(
        challenge: Challenge,
        scoreA: Int,
        scoreB: Int,
        summary: String
    ): String {

        return try {

            // Prevent duplicate posting
            val existingResult =
                db.collection("score_wall")
                    .whereEqualTo(
                        "challengeId",
                        challenge.id
                    )
                    .get()
                    .await()

            if (!existingResult.isEmpty) {
                return "Result already posted"
            }

            val winnerId =
                if (scoreA > scoreB)
                    challenge.hostTeamId
                else
                    challenge.acceptedTeamId ?: ""

            val winnerName =
                if (scoreA > scoreB)
                    challenge.hostTeamName
                else
                    challenge.acceptedTeamName ?: ""

            val result =
                MatchResult(

                    challengeId =
                        challenge.id,

                    sport =
                        challenge.sport,

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

                    hostScore =
                        scoreA,

                    opponentScore =
                        scoreB,

                    winnerTeamId =
                        winnerId,

                    winnerTeamName =
                        winnerName,

                    summary =
                        summary,

                    groundName =
                        challenge.groundName,

                    date =
                        challenge.date
                )

            db.collection("score_wall")
                .add(result)
                .await()

            // Mark challenge completed
            db.collection("challenges")
                .document(challenge.id)
                .update(
                    "status",
                    "COMPLETED"
                )
                .await()

            "Match Result Posted"

        } catch (e: Exception) {

            e.printStackTrace()

            e.message
                ?: "Failed to post result"
        }
    }

    // ==========================
// POST MATCH RESULT
// ==========================
    suspend fun postResult(
        challenge: Challenge,
        scoreA: Int,
        scoreB: Int,
        summary: String
    ): String {

        return try {

            val winnerId =
                if (scoreA > scoreB) {
                    challenge.hostTeamId
                } else {
                    challenge.acceptedTeamId
                        ?: ""
                }

            val winnerName =
                if (scoreA > scoreB) {
                    challenge.hostTeamName
                } else {
                    challenge.acceptedTeamName
                        ?: ""
                }

            val result =
                hashMapOf(

                    "challengeId" to
                            challenge.id,

                    "sport" to
                            challenge.sport,

                    "teamAId" to
                            challenge.hostTeamId,

                    "teamAName" to
                            challenge.hostTeamName,

                    "teamBId" to
                            (
                                    challenge.acceptedTeamId
                                        ?: ""
                                    ),

                    "teamBName" to
                            (
                                    challenge.acceptedTeamName
                                        ?: ""
                                    ),

                    "scoreA" to
                            scoreA,

                    "scoreB" to
                            scoreB,

                    "winnerId" to
                            winnerId,

                    "winnerName" to
                            winnerName,

                    "summary" to
                            summary,

                    "groundName" to
                            challenge.groundName,

                    "date" to
                            challenge.date,

                    "createdAt" to
                            System.currentTimeMillis()
                )

            // Save result
            db.collection(
                "match_results"
            )
                .add(result)
                .await()

            // Update challenge status
            db.collection(
                "challenges"
            )
                .document(
                    challenge.id
                )
                .update(
                    "status",
                    "COMPLETED"
                )
                .await()

            "Result Posted"

        } catch (
            e: Exception
        ) {

            e.printStackTrace()

            e.message
                ?: "Failed"
        }
    }

}