package com.geocue.android.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.geocue.android.domain.model.NotificationMode

@Entity(tableName = "geofences")
data class GeofenceEntity(
    @PrimaryKey val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val radius: Float,
    val entryMessage: String,
    val exitMessage: String,
    val notifyOnEntry: Boolean,
    val notifyOnExit: Boolean,
    val isEnabled: Boolean,
    val notificationMode: NotificationMode,
    val createdAt: Long
)
