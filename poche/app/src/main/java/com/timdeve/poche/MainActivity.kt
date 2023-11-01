package com.timdeve.poche

import android.app.Fragment
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.MainThread
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelLazy
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.franmontiel.persistentcookiejar.ClearableCookieJar
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.timdeve.poche.network.StoriesApi
import com.timdeve.poche.ui.screens.HomeScreen
import com.timdeve.poche.ui.screens.StoriesViewModel
import com.timdeve.poche.ui.theme.PocheTheme
import okhttp3.OkHttpClient
import okhttp3.OkHttpClient.Builder


@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val cookieJar: ClearableCookieJar =
            PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(this))

        val httpClient: OkHttpClient = Builder()
            .cookieJar(cookieJar)
            .build()

        val storyApi = StoriesApi(httpClient)

        setContent {
            PocheTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = colorScheme.surfaceColorAtElevation(2.dp)
                ) {
                    val storiesViewModel by lazy {
                        injectViewModel { StoriesViewModel(storyApi) }
                    }
                    HomeScreen(storiesUiState = storiesViewModel.storiesUiState)
                }
            }
        }
    }

    private inline fun <reified T: ViewModel> injectViewModel(crossinline lambda: () -> T): T {
        return ViewModelProvider(this, createWithFactory { lambda() })[T::class.java]
    }
}

fun createWithFactory(
    create: () -> ViewModel
): ViewModelProvider.Factory {
    return object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")// Casting T as ViewModel
            return create.invoke() as T
        }
    }
}
