package com.timdeve.poche.ui.screens.article

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.timdeve.poche.persistence.Article
import com.timdeve.poche.repository.ArticlesRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException


sealed interface ArticleUiState {
    data class Success(val article: Article) : ArticleUiState
    data object Loading : ArticleUiState
    data object Error : ArticleUiState
}

@Suppress("UNCHECKED_CAST")
class ArticleModelFactory(
    private val articlesRepo: ArticlesRepository,
    private val articleUrl: String
) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ArticleViewModel(articlesRepo, articleUrl) as T
}

class ArticleViewModel(
    private val articlesRepo: ArticlesRepository,
    private val articleUrl: String
) :
    ViewModel() {
    var articleUiState: ArticleUiState by mutableStateOf(ArticleUiState.Loading)
        private set

    init {
        getArticle()
    }

    private fun getArticle() {
        viewModelScope.launch {
            articleUiState = ArticleUiState.Loading
            try {
                articlesRepo.fetchArticle(articleUrl)
                articlesRepo.getArticle(articleUrl).collect {
                    articleUiState = ArticleUiState.Success(it)
                }
            } catch (e: IOException) {
                if (e is SocketTimeoutException) {
                    Log.e("Poche", "Socket timeout fallback to cache")
                    articlesRepo.getArticle(articleUrl).collect {
                        articleUiState = ArticleUiState.Success(it)
                    }
                } else {
                    Log.e("Poche", e.toString())
                    articleUiState = ArticleUiState.Error
                }
            } catch (e: HttpException) {
                Log.e("Poche", e.toString())
                articleUiState = ArticleUiState.Error
            }
        }
    }
}