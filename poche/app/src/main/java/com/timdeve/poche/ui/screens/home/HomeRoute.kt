package com.timdeve.poche.ui.screens.home

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeRoute(storiesViewModel: StoriesViewModel) {
    HomeScreen(
        storiesUiState = storiesViewModel.storiesUiState,
        getStories = storiesViewModel::getStories
    )
}