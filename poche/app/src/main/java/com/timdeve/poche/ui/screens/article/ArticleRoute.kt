package com.timdeve.poche.ui.screens.article

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.timdeve.poche.repository.ArticlesRepository

@Composable
fun ArticleRoute(
    articleApi: ArticlesRepository,
    pageUrl: String,
    navController: NavHostController
) {
    val articleViewModel: ArticleViewModel =
        viewModel(factory = ArticleModelFactory(articleApi, pageUrl))
    ArticleScreen(articleViewModel.articleUiState, navController::navigateUp)
}