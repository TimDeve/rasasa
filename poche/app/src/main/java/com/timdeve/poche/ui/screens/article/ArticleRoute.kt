package com.timdeve.poche.ui.screens.article

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.timdeve.poche.network.ArticleApiService

@Composable
fun ArticleRoute(articleApi: ArticleApiService, pageUrl: String, navController: NavHostController) {
    val articleViewModel: ArticleViewModel = viewModel(factory = ArticleModelFactory(articleApi, pageUrl))
    ArticleScreen(articleViewModel.articleUiState, navController::navigateUp)
}