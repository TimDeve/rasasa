package com.timdeve.poche

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.franmontiel.persistentcookiejar.ClearableCookieJar
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.timdeve.poche.network.ArticleApi
import com.timdeve.poche.network.FeedsApi
import com.timdeve.poche.network.LoginApi
import com.timdeve.poche.network.StoriesApi
import com.timdeve.poche.persistence.PocheDatabase
import com.timdeve.poche.repository.ArticlesRepository
import com.timdeve.poche.repository.FeedsRepository
import com.timdeve.poche.ui.screens.feedlists.FeedsViewModel
import com.timdeve.poche.ui.screens.login.AuthStatus
import com.timdeve.poche.ui.screens.login.AuthViewModel
import com.timdeve.poche.ui.screens.stories.StoriesViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import okhttp3.OkHttpClient
import okhttp3.OkHttpClient.Builder
import okhttp3.Response
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val authStatus = MutableStateFlow<AuthStatus>(AuthStatus.LoggedIn)

        val cookieJar: ClearableCookieJar =
            PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(this))

        val httpClient: OkHttpClient = Builder()
            .addInterceptor { chain ->
                val response: Response = chain.proceed(chain.request())
                if (response.code == 401 && authStatus.value is AuthStatus.LoggedIn) {
                    authStatus.update { AuthStatus.LoggedOff }
                    response
                } else response
            }
            .connectTimeout(1.seconds.toJavaDuration())
            .cookieJar(cookieJar)
            .build()

        val db = Room.databaseBuilder(
            applicationContext,
            PocheDatabase::class.java, "database-name"
        ).build()

        val loginApi = LoginApi(httpClient)
        val authViewModel by lazy { injectViewModel { AuthViewModel(loginApi, authStatus) } }

        val feedsApi = FeedsApi(httpClient)
        val feedsRepository = FeedsRepository(db.feedListsDao(), feedsApi)
        val feedsViewModel by lazy { injectViewModel { FeedsViewModel(feedsApi, feedsRepository) } }

        val storyApi = StoriesApi(httpClient)
        val storiesViewModel by lazy { injectViewModel { StoriesViewModel(storyApi) } }

        val articleApi = ArticleApi(httpClient)
        val articlesRepository = ArticlesRepository(db.articlesDao(), articleApi)

        setContent {
            PocheApp(storiesViewModel, authViewModel, feedsViewModel, articlesRepository)
        }
    }

    private inline fun <reified T : ViewModel> injectViewModel(crossinline lambda: () -> T): T {
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
