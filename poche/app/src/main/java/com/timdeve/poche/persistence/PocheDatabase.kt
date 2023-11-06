package com.timdeve.poche.persistence

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Article::class, Feed::class, FeedList::class, FeedListFeedCrossRef::class],
    version = 1
)
abstract class PocheDatabase : RoomDatabase() {
    abstract fun articlesDao(): ArticlesDao
    abstract fun feedListsDao(): FeedListsDao
}
