package com.timdeve.poche

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.timdeve.poche.network.ArticleApi
import com.timdeve.poche.ui.screens.feedlists.FeedsViewModel
import com.timdeve.poche.ui.screens.login.AuthViewModel
import com.timdeve.poche.ui.screens.stories.StoriesViewModel
import com.timdeve.poche.ui.theme.PocheTheme

@Composable
fun PocheApp(
    storiesViewModel: StoriesViewModel,
    authViewModel: AuthViewModel,
    feedsViewModel: FeedsViewModel,
    articleApi: ArticleApi,
) {
    val navController = rememberNavController()
    BaseWrapper {
        PocheNavGraph(storiesViewModel, authViewModel, feedsViewModel, articleApi, navController)
    }
}

@Composable
fun BaseWrapper(content: @Composable () -> Unit) {
    PocheTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = colorScheme.surfaceColorAtElevation(2.dp),
        ) {
            content()
        }
    }
}