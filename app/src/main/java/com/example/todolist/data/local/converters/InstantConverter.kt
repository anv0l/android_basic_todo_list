package com.example.todolist.data.local.converters

import androidx.room.TypeConverter
import java.time.Instant

class InstantConverter {
    @TypeConverter
    fun fromInstant(instant: Instant?): Long? {
        return instant?.toEpochMilli()
    }

    @TypeConverter
    fun toInstant(millis: Long?): Instant {
        return Instant.ofEpochMilli(millis ?: 0)
    }
}