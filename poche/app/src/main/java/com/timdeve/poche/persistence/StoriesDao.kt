package com.timdeve.poche.persistence

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.datetime.Instant
import java.net.URL

@Entity(tableName = "stories")
data class Story(
    @PrimaryKey
    val id: Long,
    @ColumnInfo(name = "feed_id")
    val feedId: Long,
    val title: String,
    val url: URL,
    @ColumnInfo(name = "is_read")
    var isRead: Boolean,
    @ColumnInfo(name = "published_date")
    val publishedDate: Instant,
    val content: String,
) {
    companion object
}

fun Story.Companion.fromModel(story: com.timdeve.poche.model.Story): Story {
    return Story(
        story.id,
        story.feedId,
        story.title,
        story.url,
        story.isRead,
        story.publishedDate,
        story.content,
    )
}

fun Story.toModel(): com.timdeve.poche.model.Story {
    return com.timdeve.poche.model.Story(
        this.id,
        this.feedId,
        this.title,
        this.url,
        this.isRead,
        this.publishedDate,
        this.content,
    )
}

@Dao
interface StoriesDao {
    @Query(
        """
        select * 
        from stories 
        where is_read == coalesce(:read, 0)
           or is_read == 0
        order by published_date desc
        limit 500
    """
    )
    suspend fun getStories(read: Boolean): List<Story>

    @Query(
        """
        select *
        from stories as s
        where exists(
            select * 
            from feed_list_feed_cross_refs as refs 
            where refs.feed_id == s.feed_id 
              and refs.feed_list_id == :listId
        )
        and (
            is_read == coalesce(:read, 0)
         or is_read == 0
        )
        order by published_date desc
        limit 500
    """
    )
    suspend fun getStoriesByListId(listId: Long, read: Boolean): List<Story>

    @Query(
        """
        select * 
        from stories as s
        where exists(
            select * 
            from articles as a
            where a.url == s.url
        )
        and (
            is_read == coalesce(:read, 0)
         or is_read == 0
        )
        order by published_date desc
        limit 500
    """
    )
    suspend fun getCachedStories(read: Boolean): List<Story>

    @Query(
        """
        select *
        from stories as s
        where exists(
            select * 
            from feed_list_feed_cross_refs as refs 
            where refs.feed_id == s.feed_id 
              and refs.feed_list_id == :listId
        )
        and exists(
            select * 
            from articles as a
            where a.url == s.url
        )
        and (
            is_read == coalesce(:read, 0)
         or is_read == 0
        )
        order by published_date desc
        limit 500
    """
    )
    suspend fun getCachedStoriesByListId(listId: Long, read: Boolean): List<Story>

    @Query("update stories set is_read = 1 where id == :id")
    suspend fun markStoryAsRead(id: Long)

    @Query("update stories set is_read = 1 where id IN (:ids)")
    suspend fun markStoriesAsRead(ids: List<Long>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStories(stories: List<Story>)

    @Query("delete from stories where published_date < :date")
    suspend fun deleteStoriesOlderThan(date: Instant)
}