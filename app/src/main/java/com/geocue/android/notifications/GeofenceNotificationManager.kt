package com.geocue.android.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.geocue.android.MainActivity
import com.geocue.android.R
import com.geocue.android.domain.model.GeofenceLocation
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

class GeofenceNotificationManager(
    private val context: Context,
    private val channels: NotificationChannels
) {
    private val notificationManager = NotificationManagerCompat.from(context)
    private val throttledEvents = ConcurrentHashMap<String, Instant>()

    fun notifyEvent(location: GeofenceLocation, transitionType: GeofenceTransition) {
        if (!notificationManager.areNotificationsEnabled()) return

        channels.ensureChannels()

        val message = when (transitionType) {
            GeofenceTransition.ENTRY -> if (location.entryMessage.isNotBlank()) {
                location.entryMessage
            } else {
                "You arrived at ${location.name}"
            }
            GeofenceTransition.EXIT -> if (location.exitMessage.isNotBlank()) {
                location.exitMessage
            } else {
                "You left ${location.name}"
            }
        }

        val throttleKey = "${location.id}:${transitionType.name}"
        val lastSent = throttledEvents[throttleKey]
        if (lastSent != null && Instant.now().minusSeconds(60).isBefore(lastSent)) {
            return
        }

        throttledEvents[throttleKey] = Instant.now()

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channels.geofenceChannelId)
            .setSmallIcon(R.drawable.ic_stat_geofence)
            .setContentTitle(location.name)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(Random.nextInt(), notification)
    }
}

enum class GeofenceTransition { ENTRY, EXIT }
