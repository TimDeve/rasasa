package com.timdeve.poche.network

import android.content.Context
import com.franmontiel.persistentcookiejar.ClearableCookieJar
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.timdeve.poche.model.Story
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.http.GET
import java.security.AccessController.getContext

private const val BASE_URL =
    "http://10.0.2.2:8091"

/**
 * Use the Retrofit builder to build a retrofit object using a kotlinx.serialization converter
 */

@Serializable
data class GetStoriesResponse(
    val stories: List<Story>
)

interface StoryApiService {
    @GET("v0/stories")
    suspend fun getStories(): GetStoriesResponse
}

private val retrofit = Retrofit.Builder()
    .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
//        .client(client)
    .baseUrl(BASE_URL)
    .build()

class StoriesApi(val client: OkHttpClient) {

    val retrofitService: StoryApiService by lazy {
        retrofit.create(StoryApiService::class.java)
    }
}
