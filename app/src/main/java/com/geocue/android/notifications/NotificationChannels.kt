package com.geocue.android.notifications

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class NotificationChannels(private val app: Application) {
    val geofenceChannelId = "geofence_events"

    fun ensureChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = app.getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                geofenceChannelId,
                "Geofence Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications when entering or exiting saved locations"
            }
            manager?.createNotificationChannel(channel)
        }
    }
}
