package com.timdeve.poche.ui.screens.feedlists

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.timdeve.poche.ui.screens.article.FeedListsScreen

@Composable
fun FeedListsRoute(
    feedsViewModel: FeedsViewModel,
    navController: NavHostController,
) {
    FeedListsScreen(feedsViewModel.feedsUiState, navController)
}