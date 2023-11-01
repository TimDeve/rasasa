package com.timdeve.poche.ui.screens

import android.os.Build
import retrofit2.HttpException
import androidx.annotation.RequiresExtension
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timdeve.poche.model.Story
import com.timdeve.poche.network.StoriesApi
import com.timdeve.poche.network.StoryApiService
import kotlinx.coroutines.launch
import java.io.IOException

sealed interface StoriesUiState {
    data class Success(val stories: List<Story>) : StoriesUiState
    object Error : StoriesUiState
    object Loading : StoriesUiState
}
class StoriesViewModel(private val storyApi: StoriesApi) : ViewModel() {
    var storiesUiState: StoriesUiState by mutableStateOf(StoriesUiState.Loading)
        private set

    init {
        getStories()
    }

    fun getStories() {
        viewModelScope.launch {
            storiesUiState = StoriesUiState.Loading
            storiesUiState = try {
                val storiesRes = storyApi.retrofitService.getStories()
                StoriesUiState.Success(storiesRes.stories)
            } catch (e: IOException) {
                StoriesUiState.Error
            } catch (e: HttpException) {
                StoriesUiState.Error
            }

        }
    }
}