package com.timdeve.poche.ui.screens.feedlists

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timdeve.poche.model.Feed
import com.timdeve.poche.model.FeedList
import com.timdeve.poche.network.FeedsApiService
import com.timdeve.poche.ui.screens.story.FeedListsScreen
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException


sealed interface FeedsUiState {
    data class Success(val feeds: Map<Int, Feed>, val feedLists: List<FeedList>) : FeedsUiState
    data class Loading(val feeds: Map<Int, Feed>, val feedLists: List<FeedList>) : FeedsUiState
    data object Error : FeedsUiState
}

class FeedsViewModel(private val feedsApi: FeedsApiService) : ViewModel() {
    private var feeds: Map<Int,Feed> by mutableStateOf(emptyMap())
    private var feedLists: List<FeedList> by mutableStateOf(emptyList())

    var feedsUiState: FeedsUiState by mutableStateOf(FeedsUiState.Loading(feeds, feedLists))
        private set

    init {
        getFeedsAndFeedLists()
    }

    fun getFeedsAndFeedLists() {
        viewModelScope.launch {
            feedsUiState = try {
                FeedsUiState.Loading(feeds, feedLists)
                val feedsDeferred = async { feedsApi.getFeeds() }
                val feedListsDeferred = async { feedsApi.getFeedLists() }
                feeds = feedsDeferred.await().feeds.associateBy { it.id }
                feedLists = feedListsDeferred.await().lists
                FeedsUiState.Success(feeds, feedLists)
            } catch (e: IOException) {
                Log.e("Poche", e.toString())
                FeedsUiState.Error
            } catch (e: HttpException) {
                Log.e("Poche", e.toString())
                FeedsUiState.Error
            }

        }
    }
}