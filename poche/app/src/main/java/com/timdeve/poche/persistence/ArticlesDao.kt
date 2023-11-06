package com.timdeve.poche.persistence

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "articles")
data class Article(
    @PrimaryKey
    val url: String,
    val readable: Boolean,
    val title: String = "",
    val byline: String? = "",
    val content: String = "",
) {
    companion object
}

fun Article.Companion.fromModel(article: com.timdeve.poche.model.Article): Article {
    return Article(
        url = article.url,
        readable = article.readable,
        title = article.title,
        byline = article.byline,
        content = article.content,
    )
}

fun Article.toModel(): com.timdeve.poche.model.Article {
    return com.timdeve.poche.model.Article(
        url = this.url,
        readable = this.readable,
        title = this.title,
        byline = this.byline,
        content = this.content,
    )
}

@Dao
interface ArticlesDao {
    @Query("select * from articles where url=:url")
    fun getArticle(url: String): Flow<Article>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticle(article: Article)
}