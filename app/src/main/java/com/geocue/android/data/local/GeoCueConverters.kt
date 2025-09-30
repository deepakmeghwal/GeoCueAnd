package com.geocue.android.data.local

import androidx.room.TypeConverter
import com.geocue.android.domain.model.NotificationMode
import java.time.Instant

class GeoCueConverters {
    @TypeConverter
    fun fromInstant(instant: Instant?): Long? = instant?.toEpochMilli()

    @TypeConverter
    fun toInstant(millis: Long?): Instant? = millis?.let { Instant.ofEpochMilli(it) }

    @TypeConverter
    fun fromNotificationMode(mode: NotificationMode?): String? = mode?.name

    @TypeConverter
    fun toNotificationMode(name: String?): NotificationMode? = name?.let { NotificationMode.valueOf(it) }
}
