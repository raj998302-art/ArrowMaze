package com.zenox.arrowmaze.data.repository

import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.zenox.arrowmaze.core.util.Constants
import com.zenox.arrowmaze.domain.model.LeaderboardEntry
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LeaderboardRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    private val playersCollection = firestore.collection("players")

    suspend fun getGlobalLeaderboard(
        period: Constants.LeaderboardPeriod,
        limit: Int = 20
    ): Result<List<LeaderboardEntry>> = runCatching {
        val orderByField = when (period) {
            Constants.LeaderboardPeriod.WEEKLY -> "weeklyXp"
            Constants.LeaderboardPeriod.MONTHLY -> "monthlyXp"
            Constants.LeaderboardPeriod.ALL_TIME -> "totalXp"
        }

        val snapshot = playersCollection
            .orderBy(orderByField, Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get()
            .await()

        snapshot.documents.mapIndexed { index, doc ->
            LeaderboardEntry(
                uid = doc.id,
                rank = index + 1,
                playerName = doc.getString("playerName") ?: doc.getString("nickname") ?: "Unknown",
                avatarUrl = doc.getString("avatarUrl") ?: "",
                country = doc.getString("country") ?: "Global",
                level = doc.getLong("level")?.toInt() ?: 0,
                coins = doc.getLong("coins")?.toInt() ?: 0,
                xp = doc.getLong(orderByField) ?: 0L
            )
        }
    }

    suspend fun getFriendsLeaderboard(limit: Int = 20): Result<List<LeaderboardEntry>> = runCatching {
        val currentUserUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
            ?: throw IllegalStateException("User not authenticated")

        // Get the current user's friends list
        val friendsDoc = firestore.collection("friends").document(currentUserUid).get().await()
        val friendUids = (friendsDoc.get("friendUids") as? List<*>)?.mapNotNull { it.toString() }
            ?: return@runCatching emptyList()

        if (friendUids.isEmpty()) return@runCatching emptyList()

        // Fetch friends' data in a single batch query
        val snapshot = playersCollection
            .whereIn(FieldPath.documentId(), friendUids)
            .orderBy("totalXp", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get()
            .await()

        snapshot.documents.mapIndexed { index, doc ->
            LeaderboardEntry(
                uid = doc.id,
                rank = index + 1,
                playerName = doc.getString("playerName") ?: doc.getString("nickname") ?: "Unknown",
                avatarUrl = doc.getString("avatarUrl") ?: "",
                country = doc.getString("country") ?: "Global",
                level = doc.getLong("level")?.toInt() ?: 0,
                coins = doc.getLong("coins")?.toInt() ?: 0,
                xp = doc.getLong("totalXp") ?: 0L
            )
        }.sortedByDescending { it.xp }
    }

    suspend fun updateLeaderboardEntry(
        uid: String,
        playerName: String,
        level: Int,
        coins: Int,
        xp: Long,
        avatarUrl: String,
        country: String
    ): Result<Unit> = runCatching {
        playersCollection.document(uid).update(
            mapOf(
                "playerName" to playerName,
                "level" to level,
                "coins" to coins,
                "totalXp" to xp,
                "weeklyXp" to xp, // simplified; real impl tracks period
                "monthlyXp" to xp,
                "avatarUrl" to avatarUrl,
                "country" to country
            )
        ).await()
    }
}