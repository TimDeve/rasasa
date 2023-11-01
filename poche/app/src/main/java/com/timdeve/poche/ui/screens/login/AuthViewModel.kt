package com.timdeve.poche.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timdeve.poche.network.LoginApi
import com.timdeve.poche.network.LoginApiService
import com.timdeve.poche.network.LoginRequest
import com.timdeve.poche.network.StoryApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

sealed interface AuthStatus {
    data object LoggedOff : AuthStatus
    data object LoggedIn : AuthStatus
}

class AuthViewModel(
    private val loginService: LoginApi,
    private val _authStatus: MutableStateFlow<AuthStatus>
) : ViewModel() {
    val authStatus: StateFlow<AuthStatus> = _authStatus.asStateFlow()

    fun login(req: LoginRequest) {
        viewModelScope.launch {
            _authStatus.update {
                try {
                    loginService.retrofitService.login(req)
                    AuthStatus.LoggedIn
                } catch (e: IOException) {
                    AuthStatus.LoggedOff
                } catch (e: HttpException) {
                    AuthStatus.LoggedOff
                }
            }
        }
    }
}