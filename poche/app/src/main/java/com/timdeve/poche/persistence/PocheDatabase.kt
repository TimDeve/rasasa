package com.timdeve.poche.persistence

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

const val DB_NAME = "poche-db"

@Database(
    entities = [
        Article::class,
        Feed::class,
        FeedList::class,
        FeedListFeedCrossRef::class,
        Story::class
    ],
    version = 2,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ]
)

@TypeConverters(Converters::class)
abstract class PocheDatabase : RoomDatabase() {
    abstract fun articlesDao(): ArticlesDao
    abstract fun feedListsDao(): FeedListsDao
    abstract fun storiesDao(): StoriesDao

    companion object {
        fun make(ctx: Context): PocheDatabase {
            return Room.databaseBuilder(
                ctx,
                PocheDatabase::class.java,
                DB_NAME
            ).build()
        }
    }
}
