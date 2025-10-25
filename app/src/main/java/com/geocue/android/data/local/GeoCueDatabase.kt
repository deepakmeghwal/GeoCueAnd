package com.geocue.android.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        GeofenceEntity::class,
        NotificationHistoryEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(GeoCueConverters::class)
abstract class GeoCueDatabase : RoomDatabase() {
    abstract fun geofenceDao(): GeofenceDao
    abstract fun notificationHistoryDao(): NotificationHistoryDao
}
