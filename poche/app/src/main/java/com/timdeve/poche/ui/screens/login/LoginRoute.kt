package com.timdeve.poche.ui.screens.login

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable

@ExperimentalMaterialApi
@ExperimentalMaterial3Api
@Composable
fun LoginRoute(authViewModel: AuthViewModel) {
    LoginScreen(authViewModel::login)
}
