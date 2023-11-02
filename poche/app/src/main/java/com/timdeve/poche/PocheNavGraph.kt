package com.timdeve.poche

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.timdeve.poche.network.ArticleApiService
import com.timdeve.poche.ui.screens.article.ArticleRoute
import com.timdeve.poche.ui.screens.feedlists.FeedsViewModel
import com.timdeve.poche.ui.screens.home.HomeRoute
import com.timdeve.poche.ui.screens.home.StoriesViewModel
import com.timdeve.poche.ui.screens.login.AuthStatus
import com.timdeve.poche.ui.screens.login.AuthViewModel
import com.timdeve.poche.ui.screens.login.LoginRoute
import java.net.URLDecoder


@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PocheNavGraph(
    storiesViewModel: StoriesViewModel,
    authViewModel: AuthViewModel,
    feedsViewModel: FeedsViewModel,
    articleApiService: ArticleApiService,
    navController: NavHostController = rememberNavController()
) {
    NavHost(startDestination = PocheDestinations.HOME_ROUTE, navController = navController) {
        composable(PocheDestinations.HOME_ROUTE) {
            AuthWall(authViewModel, navController) {
                HomeRoute(storiesViewModel, feedsViewModel, navController)
            }
        }
        composable(
//            "article/{pageUrl}"
            route = "article/{pageUrl}",
            arguments = listOf(
                navArgument("pageUrl") { type = NavType.StringType }
            )
        ) {
            AuthWall(authViewModel, navController) {
                val encodedUrl = it.arguments?.getString("pageUrl")
                ArticleRoute(
                    articleApiService,
                    URLDecoder.decode(encodedUrl, "UTF-8"),
                    navController
                )
            }
        }
        composable(PocheDestinations.LOGIN_ROUTE) {
            val authStatus by authViewModel.authStatus.collectAsStateWithLifecycle()
            if (authStatus is AuthStatus.LoggedIn) {
                LaunchedEffect(key1 = authStatus) {
                    navController.navigate(PocheDestinations.HOME_ROUTE)
                }
            } else {
//                AuthWall(authViewModel, navController, AuthStatus.LoggedOff) {
                LoginRoute(authViewModel)
//                }
            }
        }
    }
}

@Composable
fun AuthWall(
    authViewModel: AuthViewModel,
    navController: NavHostController,
    requirement: AuthStatus = AuthStatus.LoggedIn,
    content: @Composable () -> Unit
) {
    val authStatus by authViewModel.authStatus.collectAsStateWithLifecycle()

    if (authStatus != requirement) {
        LaunchedEffect(key1 = authStatus) {
            if (requirement is AuthStatus.LoggedIn) {
                navController.navigate(PocheDestinations.LOGIN_ROUTE)
            } else {
                navController.navigate(PocheDestinations.HOME_ROUTE)
            }
        }
    } else {
        content()
    }
}

