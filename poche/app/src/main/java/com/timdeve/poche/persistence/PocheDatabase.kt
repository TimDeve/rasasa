package com.timdeve.poche.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        Article::class,
        Feed::class,
        FeedList::class,
        FeedListFeedCrossRef::class,
        Story::class
    ],
    version = 1
)
@TypeConverters(Converters::class)
abstract class PocheDatabase : RoomDatabase() {
    abstract fun articlesDao(): ArticlesDao
    abstract fun feedListsDao(): FeedListsDao
    abstract fun storiesDao(): StoriesDao
}
