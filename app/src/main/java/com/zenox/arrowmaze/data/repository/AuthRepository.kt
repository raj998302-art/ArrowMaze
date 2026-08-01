package com.zenox.arrowmaze.data.repository

import android.net.Uri
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    val currentUser: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    val isAnonymous: Boolean
        get() = firebaseAuth.currentUser?.isAnonymous == true

    suspend fun signInAnonymously(): Result<FirebaseUser> = runCatching {
        firebaseAuth.signInAnonymously().await().user
            ?: throw IllegalStateException("Anonymous sign-in returned null user")
    }

    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> = runCatching {
        firebaseAuth.signInWithEmailAndPassword(email, password).await().user
            ?: throw IllegalStateException("Email sign-in returned null user")
    }

    suspend fun signUpWithEmail(email: String, password: String): Result<FirebaseUser> = runCatching {
        firebaseAuth.createUserWithEmailAndPassword(email, password).await().user
            ?: throw IllegalStateException("Email sign-up returned null user")
    }

    suspend fun signInWithGoogle(googleIdToken: String): Result<FirebaseUser> = runCatching {
        val credential = GoogleAuthProvider.getCredential(googleIdToken, null)
        firebaseAuth.signInWithCredential(credential).await().user
            ?: throw IllegalStateException("Google sign-in returned null user")
    }

    suspend fun linkGoogleAccount(googleIdToken: String): Result<FirebaseUser> = runCatching {
        val user = firebaseAuth.currentUser
            ?: throw IllegalStateException("No user currently signed in")
        val credential = GoogleAuthProvider.getCredential(googleIdToken, null)
        user.linkWithCredential(credential).await().user
            ?: throw IllegalStateException("Google account linking returned null user")
    }

    suspend fun linkEmailAccount(email: String, password: String): Result<FirebaseUser> = runCatching {
        val user = firebaseAuth.currentUser
            ?: throw IllegalStateException("No user currently signed in")
        val credential = EmailAuthProvider.getCredential(email, password)
        user.linkWithCredential(credential).await().user
            ?: throw IllegalStateException("Email account linking returned null user")
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> = runCatching {
        firebaseAuth.sendPasswordResetEmail(email).await()
    }

    suspend fun sendEmailVerification(): Result<Unit> = runCatching {
        val user = firebaseAuth.currentUser
            ?: throw IllegalStateException("No user currently signed in")
        user.sendEmailVerification().await()
    }

    suspend fun signOut() {
        firebaseAuth.signOut()
    }

    suspend fun deleteAccount(): Result<Unit> = runCatching {
        val user = firebaseAuth.currentUser
            ?: throw IllegalStateException("No user currently signed in")
        user.delete().await()
    }

    fun getUserUid(): String? = firebaseAuth.currentUser?.uid

    suspend fun updateProfile(nickname: String, country: String, bio: String): Result<Unit> = runCatching {
        val user = firebaseAuth.currentUser
            ?: throw IllegalStateException("No user currently signed in")

        val profileUpdates = userProfileChangeRequest {
            displayName = nickname
        }
        user.updateProfile(profileUpdates).await()

        // Also store extra fields in Firestore
        val uid = user.uid
        firestore.collection("players").document(uid).update(
            mapOf(
                "nickname" to nickname,
                "country" to country,
                "bio" to bio,
                "playerName" to nickname
            )
        ).await()
    }

    suspend fun uploadAvatar(uri: Uri): Result<String> = runCatching {
        // Avatar upload is handled by Firebase Storage; return the download URL.
        // The actual upload is typically done in a separate service, but we provide
        // the Firestore update here and the caller is expected to upload to Storage
        // and pass the resulting download URL.
        val user = firebaseAuth.currentUser
            ?: throw IllegalStateException("No user currently signed in")
        val downloadUrl = uri.toString()
        firestore.collection("players").document(user.uid).update(
            mapOf("avatarUrl" to downloadUrl)
        ).await()
        downloadUrl
    }
}
