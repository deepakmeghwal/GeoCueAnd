package com.geocue.android.domain.model

import android.location.Location
import java.time.Instant
import java.util.UUID

data class GeofenceLocation(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val address: String = "",
    val latitude: Double,
    val longitude: Double,
    val radius: Float = 150f,
    val entryMessage: String = "",
    val exitMessage: String = "",
    val notifyOnEntry: Boolean = true,
    val notifyOnExit: Boolean = false,
    val isEnabled: Boolean = true,
    val notificationMode: NotificationMode = NotificationMode.NORMAL,
    val createdAt: Instant = Instant.now()
) {
    fun distanceTo(other: Location): Float {
        val target = Location("geofence")
        target.latitude = latitude
        target.longitude = longitude
        return other.distanceTo(target)
    }
}
