package com.timdeve.poche.repository

import com.timdeve.poche.network.ArticleApiService
import com.timdeve.poche.network.swallowOfflineExceptions
import com.timdeve.poche.persistence.Article
import com.timdeve.poche.persistence.ArticlesDao
import com.timdeve.poche.persistence.fromModel

class ArticlesRepository(
    private val articlesDao: ArticlesDao,
    private val articleApiService: ArticleApiService
) {
    suspend fun getArticle(url: String): Article? {
        return articlesDao.getArticle(url)
    }

    suspend fun fetchArticle(url: String) {
        swallowOfflineExceptions {
            if (getArticle(url) == null) {
                val article = articleApiService.getArticle(url)
                articlesDao.insertArticle(Article.fromModel(article))
            }
        }
    }
}

