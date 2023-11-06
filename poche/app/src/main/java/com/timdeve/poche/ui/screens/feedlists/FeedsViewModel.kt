package com.timdeve.poche.ui.screens.feedlists

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timdeve.poche.model.Feed
import com.timdeve.poche.network.FeedsApiService
import com.timdeve.poche.persistence.FeedList
import com.timdeve.poche.repository.FeedsRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import retrofit2.HttpException
import java.io.IOException


sealed interface FeedsUiState {
    data class Success(val feeds: Map<Long, Feed>, val feedLists: Map<Long, FeedList>) :
        FeedsUiState

    data class Loading(val feeds: Map<Long, Feed>, val feedLists: Map<Long, FeedList>) :
        FeedsUiState

    data object Error : FeedsUiState
}

class FeedsViewModel(
    private val feedsApi: FeedsApiService,
    private val feedsRepository: FeedsRepository
) : ViewModel() {
    private var feeds: Map<Long, Feed> by mutableStateOf(emptyMap())
    private var feedLists: Map<Long, FeedList> by mutableStateOf(emptyMap())

    var feedsUiState: FeedsUiState by mutableStateOf(FeedsUiState.Loading(feeds, feedLists))
        private set

    init {
        getFeedsAndFeedLists()
    }

    fun getFeedsAndFeedLists() {
        viewModelScope.launch {
            supervisorScope {
                feedsUiState = FeedsUiState.Loading(feeds, feedLists)
                try {
                    val feedsDeferred = async { feedsApi.getFeeds() }
//                    val feedListsDeferred = async { feedsApi.getFeedLists() }
                    val feedListsDeferred = async { feedsRepository.fetchFeedLists() }
                    feeds = feedsDeferred.await().feeds.associateBy { it.id }
//                    feedLists = feedListsDeferred.await().lists.associateBy { it.id }
                    feedListsDeferred.await()
                    feedsRepository.getFeedLists().collect {
                        Log.d("Wow", it.toString())
                        feedLists = it.associateBy { it.id }
                        feedsUiState = FeedsUiState.Success(feeds, feedLists)
                    }
                } catch (e: IOException) {
                    Log.e("Poche", e.toString())
                    feedsUiState = FeedsUiState.Error
                } catch (e: HttpException) {
                    Log.e("Poche", e.toString())
                    feedsUiState = FeedsUiState.Error
                }
            }
        }
    }
}