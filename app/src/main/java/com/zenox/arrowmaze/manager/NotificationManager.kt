package com.zenox.arrowmaze.manager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.zenox.arrowmaze.ArrowMazeApp
import com.zenox.arrowmaze.R
import com.zenox.arrowmaze.core.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun createNotificationChannels() {
        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            createChannel(
                id = Constants.NOTIFICATION_CHANNEL_DAILY,
                name = "Daily Rewards",
                description = "Notifications for daily reward reminders",
                importance = NotificationManager.IMPORTANCE_DEFAULT
            )
            createChannel(
                id = Constants.NOTIFICATION_CHANNEL_CHALLENGE,
                name = "Challenge Reminders",
                description = "Notifications for daily challenge reminders",
                importance = NotificationManager.IMPORTANCE_HIGH
            )
            createChannel(
                id = Constants.NOTIFICATION_CHANNEL_REMINDER,
                name = "Reminders",
                description = "Come back and play reminders",
                importance = NotificationManager.IMPORTANCE_LOW
            )
            createChannel(
                id = Constants.NOTIFICATION_CHANNEL_GENERAL,
                name = "General",
                description = "General app notifications",
                importance = NotificationManager.IMPORTANCE_DEFAULT
            )
        }
    }

    fun showDailyRewardNotification(title: String, message: String) {
        val notification = buildNotification(
            channelId = Constants.NOTIFICATION_CHANNEL_DAILY,
            title = title,
            message = message
        )
        NotificationManagerCompat.from(context).notify(
            DAILY_REWARD_NOTIFICATION_ID,
            notification
        )
    }

    fun showChallengeReminderNotification(title: String, message: String) {
        val notification = buildNotification(
            channelId = Constants.NOTIFICATION_CHANNEL_CHALLENGE,
            title = title,
            message = message
        )
        NotificationManagerCompat.from(context).notify(
            CHALLENGE_REMINDER_NOTIFICATION_ID,
            notification
        )
    }

    fun showComeBackNotification(title: String, message: String) {
        val notification = buildNotification(
            channelId = Constants.NOTIFICATION_CHANNEL_REMINDER,
            title = title,
            message = message
        )
        NotificationManagerCompat.from(context).notify(
            COME_BACK_NOTIFICATION_ID,
            notification
        )
    }

    fun showSpecialEventNotification(title: String, message: String) {
        val notification = buildNotification(
            channelId = Constants.NOTIFICATION_CHANNEL_GENERAL,
            title = title,
            message = message
        )
        NotificationManagerCompat.from(context).notify(
            SPECIAL_EVENT_NOTIFICATION_ID,
            notification
        )
    }

    private fun createChannel(
        id: String,
        name: String,
        description: String,
        importance: Int
    ) {
        val channel = NotificationChannel(
            id,
            name,
            importance
        ).apply {
            this.description = description
            enableVibration(true)
        }
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun buildNotification(
        channelId: String,
        title: String,
        message: String
    ): android.app.Notification {
        val intent = Intent(context, com.zenox.arrowmaze.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
    }

    companion object {
        private const val DAILY_REWARD_NOTIFICATION_ID = 1001
        private const val CHALLENGE_REMINDER_NOTIFICATION_ID = 1002
        private const val COME_BACK_NOTIFICATION_ID = 1003
        private const val SPECIAL_EVENT_NOTIFICATION_ID = 1004
    }
}
