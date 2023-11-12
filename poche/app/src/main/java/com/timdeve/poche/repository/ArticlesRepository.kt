package com.timdeve.poche.repository

import com.timdeve.poche.network.ArticleApiService
import com.timdeve.poche.network.swallowOfflineExceptions
import com.timdeve.poche.persistence.Article
import com.timdeve.poche.persistence.ArticlesDao
import com.timdeve.poche.persistence.fromModel
import java.net.URL

class ArticlesRepository(
    private val articlesDao: ArticlesDao,
    private val articleApiService: ArticleApiService
) {
    suspend fun getArticle(url: URL): Article? {
        return articlesDao.getArticle(url)
    }

    suspend fun fetchArticle(url: URL) {
        swallowOfflineExceptions {
            if (getArticle(url) == null) {
                val article = articleApiService.getArticle(url)
                articlesDao.insertArticle(Article.fromModel(article))
            }
        }
    }

    suspend fun deleteArticlesWithoutStories() {
        articlesDao.deleteArticlesWithoutStories()
    }
}

