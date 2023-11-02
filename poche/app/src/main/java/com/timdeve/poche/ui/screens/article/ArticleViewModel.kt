package com.timdeve.poche.ui.screens.article

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.timdeve.poche.model.Article
import com.timdeve.poche.network.ArticleApiService
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException


sealed interface ArticleUiState {
    data class Success(val article: Article) : ArticleUiState
    data object Loading : ArticleUiState
    data object Error : ArticleUiState
}

@Suppress("UNCHECKED_CAST")
class ArticleModelFactory(
    private val articleApi: ArticleApiService,
    private val articleUrl: String
) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ArticleViewModel(articleApi, articleUrl) as T
}

class ArticleViewModel(private val articleApi: ArticleApiService, private val articleUrl: String) :
    ViewModel() {
    var articleUiState: ArticleUiState by mutableStateOf(ArticleUiState.Loading)
        private set

    init {
        getArticle()
    }

    fun getArticle() {
        viewModelScope.launch {
            articleUiState = ArticleUiState.Loading
            articleUiState = try {
                ArticleUiState.Success(articleApi.getArticle(articleUrl))
            } catch (e: IOException) {
                Log.e("Poche", e.toString())
                ArticleUiState.Error
            } catch (e: HttpException) {
                Log.e("Poche", e.toString())
                ArticleUiState.Error
            }
        }
    }
}