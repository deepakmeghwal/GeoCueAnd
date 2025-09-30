package com.geocue.android.data

import com.geocue.android.data.local.GeofenceDao
import com.geocue.android.data.local.GeofenceEntity
import com.geocue.android.domain.model.GeofenceLocation
import com.geocue.android.domain.model.NotificationMode
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GeofenceRepository @Inject constructor(
    private val dao: GeofenceDao
) {
    fun observeGeofences(): Flow<List<GeofenceLocation>> =
        dao.observeGeofences().map { list -> list.map { it.toDomain() } }

    suspend fun getGeofences(): List<GeofenceLocation> =
        dao.getGeofences().map { it.toDomain() }

    suspend fun upsert(location: GeofenceLocation) {
        dao.insert(location.toEntity())
    }

    suspend fun delete(location: GeofenceLocation) {
        dao.deleteById(location.id.toString())
    }

    private fun GeofenceEntity.toDomain(): GeofenceLocation = GeofenceLocation(
        id = UUID.fromString(id),
        name = name,
        address = address,
        latitude = latitude,
        longitude = longitude,
        radius = radius,
        entryMessage = entryMessage,
        exitMessage = exitMessage,
        notifyOnEntry = notifyOnEntry,
        notifyOnExit = notifyOnExit,
        isEnabled = isEnabled,
        notificationMode = notificationMode,
        createdAt = Instant.ofEpochMilli(createdAt)
    )

    private fun GeofenceLocation.toEntity(): GeofenceEntity = GeofenceEntity(
        id = id.toString(),
        name = name,
        address = address,
        latitude = latitude,
        longitude = longitude,
        radius = radius,
        entryMessage = entryMessage,
        exitMessage = exitMessage,
        notifyOnEntry = notifyOnEntry,
        notifyOnExit = notifyOnExit,
        isEnabled = isEnabled,
        notificationMode = notificationMode,
        createdAt = createdAt.toEpochMilli()
    )
}
