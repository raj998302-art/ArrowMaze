package com.zenox.arrowmaze.data.remote.firebase

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreService @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    private val playersCollection = firestore.collection("players")
    private val leaderboardCollection = firestore.collection("leaderboard")
    private val friendsCollection = firestore.collection("friends")
    private val friendRequestsCollection = firestore.collection("friendRequests")

    suspend fun savePlayerData(uid: String, data: Map<String, Any?>): Result<Unit> {
        return safeCall {
            playersCollection.document(uid).set(data, com.google.firebase.firestore.SetOptions.merge()).await()
        }
    }

    suspend fun loadPlayerData(uid: String): Result<Map<String, Any?>> {
        return safeCall {
            val snapshot = playersCollection.document(uid).get().await()
            if (snapshot.exists()) {
                snapshot.data ?: emptyMap()
            } else {
                emptyMap()
            }
        }
    }

    suspend fun updateLeaderboard(uid: String, data: Map<String, Any?>): Result<Unit> {
        return safeCall {
            leaderboardCollection.document(uid).set(data, com.google.firebase.firestore.SetOptions.merge()).await()
        }
    }

    suspend fun getLeaderboard(orderBy: String, limit: Long): Result<List<DocumentSnapshot>> {
        return safeCall {
            leaderboardCollection
                .orderBy(orderBy, com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()
                .documents
        }
    }

    suspend fun getFriends(uid: String): Result<List<DocumentSnapshot>> {
        return safeCall {
            val friendsDoc = friendsCollection.document(uid).get().await()
            val friendUids = (friendsDoc.get("friendUids") as? List<*>)
                ?.mapNotNull { it.toString() }
                ?: emptyList()
            if (friendUids.isEmpty()) {
                emptyList()
            } else {
                playersCollection
                    .whereIn(com.google.firebase.firestore.FieldPath.documentId(), friendUids)
                    .get()
                    .await()
                    .documents
            }
        }
    }

    suspend fun sendFriendRequest(fromUid: String, toUid: String, fromName: String): Result<Unit> {
        return safeCall {
            val request = mapOf(
                "fromUid" to fromUid,
                "toUid" to toUid,
                "fromName" to fromName,
                "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                "status" to "pending"
            )
            friendRequestsCollection.document("${fromUid}_${toUid}").set(request).await()
        }
    }

    suspend fun acceptFriendRequest(uid: String, friendUid: String): Result<Unit> {
        return safeCall {
            // Update the request status
            val requestId = "${friendUid}_${uid}"
            friendRequestsCollection.document(requestId).update("status", "accepted").await()

            // Add to both users' friend lists
            val userFriendsRef = friendsCollection.document(uid)
            val friendFriendsRef = friendsCollection.document(friendUid)

            firestore.runBatch { batch ->
                batch.set(userFriendsRef, mapOf(
                    "friendUids" to com.google.firebase.firestore.FieldValue.arrayUnion(friendUid)
                ), com.google.firebase.firestore.SetOptions.merge())
                batch.set(friendFriendsRef, mapOf(
                    "friendUids" to com.google.firebase.firestore.FieldValue.arrayUnion(uid)
                ), com.google.firebase.firestore.SetOptions.merge())
            }.await()
        }
    }

    suspend fun removeFriend(uid: String, friendUid: String): Result<Unit> {
        return safeCall {
            val userFriendsRef = friendsCollection.document(uid)
            val friendFriendsRef = friendsCollection.document(friendUid)

            firestore.runBatch { batch ->
                batch.set(userFriendsRef, mapOf(
                    "friendUids" to com.google.firebase.firestore.FieldValue.arrayRemove(friendUid)
                ), com.google.firebase.firestore.SetOptions.merge())
                batch.set(friendFriendsRef, mapOf(
                    "friendUids" to com.google.firebase.firestore.FieldValue.arrayRemove(uid)
                ), com.google.firebase.firestore.SetOptions.merge())
            }.await()
        }
    }

    suspend fun blockPlayer(uid: String, blockedUid: String): Result<Unit> {
        return safeCall {
            // Remove from friends first (if applicable)
            val userFriendsRef = friendsCollection.document(uid)
            val friendFriendsRef = friendsCollection.document(blockedUid)

            firestore.runBatch { batch ->
                // Remove from each other's friend lists
                batch.set(userFriendsRef, mapOf(
                    "friendUids" to com.google.firebase.firestore.FieldValue.arrayRemove(blockedUid)
                ), com.google.firebase.firestore.SetOptions.merge())
                batch.set(friendFriendsRef, mapOf(
                    "friendUids" to com.google.firebase.firestore.FieldValue.arrayRemove(uid)
                ), com.google.firebase.firestore.SetOptions.merge())
                // Add to blocked list
                batch.set(
                    firestore.collection("blocked").document(uid),
                    mapOf("blockedUids" to com.google.firebase.firestore.FieldValue.arrayUnion(blockedUid)),
                    com.google.firebase.firestore.SetOptions.merge()
                )
            }.await()
        }
    }

    suspend fun searchPlayers(query: String, limit: Int = 10): Result<List<DocumentSnapshot>> {
        return safeCall {
            playersCollection
                .whereGreaterThanOrEqualTo("nickname", query)
                .whereLessThanOrEqualTo("nickname", query + "\uf8ff")
                .limit(limit.toLong())
                .get()
                .await()
                .documents
        }
    }

    private suspend fun <T> safeCall(block: suspend () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}