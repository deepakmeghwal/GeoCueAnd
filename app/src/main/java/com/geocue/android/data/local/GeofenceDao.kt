package com.geocue.android.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GeofenceDao {
    @Query("SELECT * FROM geofences ORDER BY createdAt DESC")
    fun observeGeofences(): Flow<List<GeofenceEntity>>

    @Query("SELECT * FROM geofences ORDER BY createdAt DESC")
    suspend fun getGeofences(): List<GeofenceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(geofence: GeofenceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(geofences: List<GeofenceEntity>)

    @Update
    suspend fun update(geofence: GeofenceEntity)

    @Delete
    suspend fun delete(geofence: GeofenceEntity)

    @Query("DELETE FROM geofences WHERE id = :id")
    suspend fun deleteById(id: String)
}
