package com.timdeve.poche.persistence

import androidx.room.TypeConverter
import kotlinx.datetime.Instant
import java.net.URL

class Converters {
    @TypeConverter
    fun millisToInstant(ms: Long?): Instant? {
        return ms?.let { Instant.fromEpochMilliseconds(it) }
    }

    @TypeConverter
    fun instantToMillis(instant: Instant?): Long? {
        return instant?.toEpochMilliseconds()
    }

    @TypeConverter
    fun urlToString(url: URL?): String? {
        return url?.toString()
    }

    @TypeConverter
    fun stringToURL(s: String?): URL? {
        return s?.let { URL(it) }
    }
}
