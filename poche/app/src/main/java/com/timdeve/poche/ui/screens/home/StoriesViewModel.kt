package com.timdeve.poche.ui.screens.home

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timdeve.poche.model.Story
import com.timdeve.poche.network.StoriesApi
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

sealed interface StoriesUiState {
    data class Success(val stories: List<Story>) : StoriesUiState
    data object Error : StoriesUiState
    data class Loading(val stories: List<Story>) : StoriesUiState
}

class StoriesViewModel(private val storyApi: StoriesApi) : ViewModel() {
    var storiesUiState: StoriesUiState by mutableStateOf(StoriesUiState.Loading(emptyList()))
        private set

    private var stories: List<Story> by mutableStateOf(emptyList())

    init {
        getStories()
    }

    fun getStories() {
        viewModelScope.launch {
            storiesUiState = StoriesUiState.Loading(stories)
            storiesUiState = try {
                stories = storyApi.retrofitService.getStories().stories
                StoriesUiState.Success(stories)
            } catch (e: IOException) {
                Log.e("Poche", e.toString())
                StoriesUiState.Error
            } catch (e: HttpException) {
                Log.e("Poche", e.toString())
                StoriesUiState.Error
            }
        }
    }
}