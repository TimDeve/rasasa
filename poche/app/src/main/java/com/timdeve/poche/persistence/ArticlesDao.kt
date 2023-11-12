package com.timdeve.poche.persistence

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import java.net.URL

@Entity(tableName = "articles")
data class Article(
    @PrimaryKey
    val url: URL,
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
    suspend fun getArticle(url: URL): Article?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticle(article: Article)

    @Query("delete from articles where not exists (select * from stories where url = articles.url)")
    suspend fun deleteArticlesWithoutStories()
}