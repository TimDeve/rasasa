package com.timdeve.poche.persistence

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

@Entity(tableName = "stories")
data class Story(
    @PrimaryKey
    val id: Long,
    @ColumnInfo(name = "feed_id")
    val feedId: Long,
    val title: String,
    val url: String,
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
    @Query("select * from stories order by published_date limit 500")
    fun getStories(): Flow<List<Story>>

//    @Query("select * from stories where list_id = :listId order by published_date limit 500")
//    fun getStoriesByListId(listId: Int): Flow<List<Story>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStories(stories: List<Story>)
}