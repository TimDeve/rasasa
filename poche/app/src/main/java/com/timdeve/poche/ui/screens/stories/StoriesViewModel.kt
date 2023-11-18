package com.timdeve.poche.ui.screens.stories

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timdeve.poche.persistence.Story
import com.timdeve.poche.repository.StoriesRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

sealed interface StoriesUiState {
    data class Success(val stories: List<Story>) : StoriesUiState
    data object Error : StoriesUiState
    data class Loading(val stories: List<Story>) : StoriesUiState
}

class StoriesViewModel(
    private val storiesRepository: StoriesRepository
) : ViewModel() {
    var storiesUiState: StoriesUiState by mutableStateOf(StoriesUiState.Loading(emptyList()))
        private set

    private var stories: List<Story> by mutableStateOf(emptyList())

    var showReadStories: Boolean by mutableStateOf(false)
        private set

    var showCachedOnly: Boolean by mutableStateOf(false)
        private set

    var currentListId: Long? by mutableStateOf(-1)

    fun toggleReadStories() {
        showReadStories = !showReadStories
        getStories()
    }

    fun toggleCachedOnly() {
        showCachedOnly = !showCachedOnly
        getStories()
    }

    fun setListId(listId: Long?) {
        if (currentListId != listId) {
            currentListId = listId
            getStories()
        }
    }

    fun markStoryAsRead(index: Int) {
        viewModelScope.launch {
            stories.getOrNull(index)?.let { story ->
                try {
                    if (!story.isRead) {
                        storiesRepository.markStoriesAsRead(story.id)
                        story.isRead = true
                    }
                } catch (e: Exception) {
                    Log.e(this::class.simpleName, e.toString())
                }
                Unit
            } ?: run {
                Log.e(this::class.simpleName, "index '${index}' does not exist")
            }
        }
    }

    fun getStories() {
        storiesUiState = StoriesUiState.Loading(stories)
        viewModelScope.launch {
            try {
                stories =
                    storiesRepository.getStories(currentListId, showReadStories, showCachedOnly)
                storiesUiState = StoriesUiState.Success(stories)
            } catch (e: IOException) {
                Log.e(this::class.simpleName, e.toString())
                storiesUiState = StoriesUiState.Error
            } catch (e: HttpException) {
                Log.e(this::class.simpleName, e.toString())
                storiesUiState = StoriesUiState.Error
            }
        }
    }
}