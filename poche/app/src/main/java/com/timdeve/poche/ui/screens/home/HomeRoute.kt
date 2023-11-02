package com.timdeve.poche.ui.screens.home

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.timdeve.poche.ui.screens.feedlists.FeedsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeRoute(
    storiesViewModel: StoriesViewModel,
    feedsViewModel: FeedsViewModel,
    navController: NavHostController,
) {
    HomeScreen(
        screenTitle = "All Stories",
        storiesUiState = storiesViewModel.storiesUiState,
        getStories = storiesViewModel::getStories,
        feedsUiState = feedsViewModel.feedsUiState,
        getFeedsAndFeedLists = feedsViewModel::getFeedsAndFeedLists,
        navController = navController,
    )
}