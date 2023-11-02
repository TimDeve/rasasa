package com.timdeve.poche

import android.util.Log
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.timdeve.poche.ui.screens.feedlists.FeedsViewModel
import com.timdeve.poche.ui.screens.home.HomeRoute
import com.timdeve.poche.ui.screens.home.StoriesViewModel
import com.timdeve.poche.ui.screens.login.AuthStatus
import com.timdeve.poche.ui.screens.login.AuthViewModel
import com.timdeve.poche.ui.screens.login.LoginRoute


@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PocheNavGraph(
    storiesViewModel: StoriesViewModel,
    authViewModel: AuthViewModel,
    feedsViewModel: FeedsViewModel,
    navController: NavHostController = rememberNavController()
) {
    NavHost(startDestination = PocheDestinations.HOME_ROUTE, navController = navController) {
        composable(PocheDestinations.HOME_ROUTE) {
            AuthWall(authViewModel, navController) {
                HomeRoute(storiesViewModel, feedsViewModel)
            }
        }
        composable(PocheDestinations.LOGIN_ROUTE) {
            val authStatus by authViewModel.authStatus.collectAsStateWithLifecycle()
            if (authStatus is AuthStatus.LoggedIn) {
                LaunchedEffect(key1 = authStatus) {
                    navController.navigate(PocheDestinations.HOME_ROUTE)
                }
            } else {
                AuthWall(authViewModel, navController, AuthStatus.LoggedOff) {
                    LoginRoute(authViewModel)
                }
            }
        }
    }
}

@Composable
fun AuthWall(authViewModel: AuthViewModel, navController: NavHostController, requirement: AuthStatus = AuthStatus.LoggedIn ,content: @Composable () -> Unit) {
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

