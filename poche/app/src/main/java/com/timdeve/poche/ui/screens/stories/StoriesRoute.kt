package com.timdeve.poche.ui.screens.stories

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import com.timdeve.poche.ui.screens.feedlists.FeedsUiState
import com.timdeve.poche.ui.screens.feedlists.FeedsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoriesRoute(
    storiesViewModel: StoriesViewModel,
    feedsViewModel: FeedsViewModel,
    navController: NavHostController,
    listId: Long? = null,
) {
    LaunchedEffect(listId) {
        storiesViewModel.setListId(listId)
    }

    val screenTitle = if (listId == null) {
        "All Stories"
    } else {
        when (val uiState = feedsViewModel.feedsUiState) {
            is FeedsUiState.Success -> {
                uiState.feedLists.getOrDefault(listId, null)?.name ?: ""
            }

            else -> ""
        }
    }

    StoriesScreen(
        screenTitle = screenTitle,
        storiesUiState = storiesViewModel.storiesUiState,
        getStories = storiesViewModel::getStories,
        markStoryAsRead = storiesViewModel::markStoryAsRead,
        markStoriesAsRead = storiesViewModel::markStoriesAsRead,
        showReadStories = storiesViewModel.showReadStories,
        toggleReadStories = storiesViewModel::toggleReadStories,
        showCachedOnly = storiesViewModel.showCachedOnly,
        toggleCachedOnly = storiesViewModel::toggleCachedOnly,
        feedsUiState = feedsViewModel.feedsUiState,
        getFeedsAndFeedLists = feedsViewModel::getFeedsAndFeedLists,
        navController = navController,
    )
}