package com.timdeve.poche

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.timdeve.poche.ui.screens.feedlists.FeedsViewModel
import com.timdeve.poche.ui.screens.home.StoriesViewModel
import com.timdeve.poche.ui.screens.login.AuthViewModel
import com.timdeve.poche.ui.theme.PocheTheme

@Composable
fun PocheApp(
    storiesViewModel: StoriesViewModel,
    authViewModel: AuthViewModel,
    feedsViewModel: FeedsViewModel
) {
    PocheTheme {
        val navController = rememberNavController()
        PocheNavGraph(storiesViewModel, authViewModel, feedsViewModel, navController)
    }
}