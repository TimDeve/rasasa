package com.timdeve.poche.persistence

import androidx.room.TypeConverter
import kotlinx.datetime.Instant

class Converters {
    @TypeConverter
    fun millisToInstant(ms: Long?): Instant? {
        return ms?.let { Instant.fromEpochMilliseconds(it) }
    }

    @TypeConverter
    fun instantToMillis(instant: Instant?): Long? {
        return instant?.toEpochMilliseconds()
    }
}
