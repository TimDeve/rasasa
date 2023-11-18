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
import java.net.URL


sealed interface ArticleUiState {
    data class Success(val article: Article?) : ArticleUiState
    data object Loading : ArticleUiState
    data object Error : ArticleUiState
}

@Suppress("UNCHECKED_CAST")
class ArticleModelFactory(
    private val articlesRepo: ArticlesRepository,
    private val articleUrl: URL
) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ArticleViewModel(articlesRepo, articleUrl) as T
}

class ArticleViewModel(
    private val articlesRepo: ArticlesRepository,
    private val articleUrl: URL
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
            articleUiState = try {
                articlesRepo.fetchArticle(articleUrl)
                val article = articlesRepo.getArticle(articleUrl)
                ArticleUiState.Success(article)
            } catch (e: IOException) {
                Log.e(this::class.simpleName, e.toString())
                ArticleUiState.Error
            } catch (e: HttpException) {
                Log.e(this::class.simpleName, e.toString())
                ArticleUiState.Error
            }
        }
    }
}