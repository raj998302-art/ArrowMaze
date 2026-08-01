package com.zenox.arrowmaze.manager

import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.zenox.arrowmaze.MainActivity
import com.zenox.arrowmaze.R
import com.zenox.arrowmaze.core.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.util.Log
import javax.inject.Inject

@AndroidEntryPoint
class ArrowMazeFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var firestore: FirebaseFirestore

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "FCM token refreshed: ${token.take(16)}...")

        // Store the token in Firestore under the user's document
        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    firestore.collection("players").document(uid)
                        .update("fcmToken", token)
                        .addOnSuccessListener {
                            Log.d(TAG, "FCM token stored for user $uid")
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Failed to store FCM token: ${e.message}")
                        }
                } catch (e: Exception) {
                    Log.e(TAG, "Error storing FCM token", e)
                }
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "FCM message received from: ${message.from}")

        // Handle data messages
        val data = message.data
        if (data.isNotEmpty()) {
            handleDataMessage(data)
        }

        // Handle notification messages (display automatically if app is in background)
        message.notification?.let { notification ->
            showNotification(
                title = notification.title ?: "ArrowMaze",
                body = notification.body ?: "",
                channelId = determineChannel(data)
            )
        }
    }

    private fun handleDataMessage(data: Map<String, String>) {
        val type = data["type"] ?: return

        when (type) {
            "daily_reward" -> {
                showNotification(
                    title = data["title"] ?: "Daily Reward Ready!",
                    body = data["body"] ?: "Your daily reward is waiting. Open the app to claim it!",
                    channelId = Constants.NOTIFICATION_CHANNEL_DAILY
                )
            }
            "challenge" -> {
                showNotification(
                    title = data["title"] ?: "New Challenge!",
                    body = data["body"] ?: "A new daily challenge is available. Can you beat it?",
                    channelId = Constants.NOTIFICATION_CHANNEL_CHALLENGE
                )
            }
            "come_back" -> {
                showNotification(
                    title = data["title"] ?: "We miss you!",
                    body = data["body"] ?: "Come back and solve some puzzles!",
                    channelId = Constants.NOTIFICATION_CHANNEL_REMINDER
                )
            }
            "event" -> {
                showNotification(
                    title = data["title"] ?: "Special Event!",
                    body = data["body"] ?: "A special event is happening in ArrowMaze!",
                    channelId = Constants.NOTIFICATION_CHANNEL_GENERAL
                )
            }
            else -> {
                Log.d(TAG, "Unknown data message type: $type")
            }
        }
    }

    private fun showNotification(title: String, body: String, channelId: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationId = (System.currentTimeMillis() % 100000).toInt()
        NotificationManagerCompat.from(this).notify(notificationId, notification)
    }

    companion object { private const val TAG = "FCMService" }

    private fun determineChannel(data: Map<String, String>): String {
        return when (data["type"]) {
            "daily_reward" -> Constants.NOTIFICATION_CHANNEL_DAILY
            "challenge" -> Constants.NOTIFICATION_CHANNEL_CHALLENGE
            "come_back" -> Constants.NOTIFICATION_CHANNEL_REMINDER
            else -> Constants.NOTIFICATION_CHANNEL_GENERAL
        }
    }
}