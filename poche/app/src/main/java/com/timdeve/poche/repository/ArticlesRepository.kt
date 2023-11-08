package com.timdeve.poche.repository

import com.timdeve.poche.network.ArticleApiService
import com.timdeve.poche.network.swallowOfflineExceptions
import com.timdeve.poche.persistence.Article
import com.timdeve.poche.persistence.ArticlesDao
import com.timdeve.poche.persistence.fromModel
import kotlinx.coroutines.flow.Flow

class ArticlesRepository(
    private val articlesDao: ArticlesDao,
    private val articleApiService: ArticleApiService
) {
    fun getArticle(url: String): Flow<Article?> {
        return articlesDao.getArticle(url)
    }

    suspend fun fetchArticle(url: String) {
        swallowOfflineExceptions {
            val article = articleApiService.getArticle(url)
            articlesDao.insertArticle(Article.fromModel(article))
        }
    }
}

