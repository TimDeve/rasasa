package com.timdeve.poche.persistence

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Insert
import androidx.room.Junction
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "feed_lists")
data class FeedList(
    @PrimaryKey
    val id: Long,
    val name: String,
) {
    @Ignore
    var feedIds: List<Long> = emptyList()

    companion object
}

data class FeedListWithFeedIds(
    @PrimaryKey
    val id: Long,
    val name: String,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        entity = Feed::class,
        associateBy = Junction(
            value = FeedListFeedCrossRef::class,
            parentColumn = "feed_list_id",
            entityColumn = "feed_id"
        )
    )
    val feedIds: List<Long>
) {
    companion object
}

@Entity(tableName = "feeds")
data class Feed(
    @PrimaryKey
    val id: Long,
    val name: String,
    val url: String,
) {
    companion object
}

fun Feed.Companion.fromModel(feed: com.timdeve.poche.model.Feed): Feed {
    return Feed(
        feed.id,
        feed.name,
        feed.url,
    )
}

@Entity(
    tableName = "feed_list_feed_cross_refs",
    primaryKeys = ["feed_list_id", "feed_id"]
)
data class FeedListFeedCrossRef(
    @ColumnInfo(name = "feed_list_id")
    val feedListId: Long,
    @ColumnInfo(name = "feed_id")
    val feedId: Long,
) {
    companion object
}

fun FeedList.Companion.fromModel(list: com.timdeve.poche.model.FeedList): FeedList {
    val l = FeedList(
        list.id,
        list.name,
    )
    l.feedIds = list.feedIds
    return l
}

fun FeedList.toModel(): com.timdeve.poche.model.FeedList {
    return com.timdeve.poche.model.FeedList(
        this.id,
        this.name,
        this.feedIds,
    )
}

@Dao
abstract class FeedListsDao {
    @Transaction
    @Query("select * from feed_lists")
    abstract fun getFeedLists(): Flow<List<FeedList>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun _insertFeedLists(lists: List<FeedList>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun _insertFeedListFeedCrossRefs(crossRefs: List<FeedListFeedCrossRef>)

    @Transaction
    open suspend fun insertFeedLists(lists: List<FeedList>) {
        val crossRefs = mutableListOf<FeedListFeedCrossRef>()
        for (list in lists) {
            crossRefs.addAll(list.feedIds.map { FeedListFeedCrossRef(list.id, it) })
        }

        _insertFeedLists(lists)
        _insertFeedListFeedCrossRefs(crossRefs)
    }

    @Transaction
    @Query("select * from feeds order by name")
    abstract fun getFeeds(): Flow<List<Feed>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertFeeds(feeds: List<Feed>)
}