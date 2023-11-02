package com.timdeve.poche.ui.screens.home

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import com.timdeve.poche.ui.screens.feedlists.FeedsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeRoute(storiesViewModel: StoriesViewModel, feedsViewModel: FeedsViewModel) {
    HomeScreen(
        storiesUiState = storiesViewModel.storiesUiState,
        getStories = storiesViewModel::getStories,
        feedsUiState = feedsViewModel.feedsUiState,
        getFeedsAndFeedLists = feedsViewModel::getFeedsAndFeedLists
    )
}