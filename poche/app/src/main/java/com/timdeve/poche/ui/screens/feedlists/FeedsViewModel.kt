package com.timdeve.poche.ui.screens.feedlists

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timdeve.poche.persistence.Feed
import com.timdeve.poche.persistence.FeedList
import com.timdeve.poche.repository.FeedsRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.combine
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
                    val feedsDeferred = async { feedsRepository.fetchFeeds() }
                    val feedListsDeferred = async { feedsRepository.fetchFeedLists() }
                    feedsDeferred.await()
                    feedListsDeferred.await()
                    combine(
                        feedsRepository.getFeeds(),
                        feedsRepository.getFeedLists()
                    ) { repoFeeds, repoLists ->
                        feeds = repoFeeds.associateBy { it.id }
                        feedLists = repoLists.associateBy { it.id }
                        feedsUiState = FeedsUiState.Success(feeds, feedLists)
                    }.collect {}
                } catch (e: IOException) {
                    Log.e(this::class.simpleName, e.toString())
                    feedsUiState = FeedsUiState.Error
                } catch (e: HttpException) {
                    Log.e(this::class.simpleName, e.toString())
                    feedsUiState = FeedsUiState.Error
                }
            }
        }
    }
}