package com.timdeve.poche.network

import com.timdeve.poche.BuildConfig
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.timdeve.poche.model.Story
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.http.GET

@Serializable
data class GetStoriesResponse(
    val stories: List<Story>
)

interface StoryApiService {
    @GET("api/v0/stories")
    suspend fun getStories(): GetStoriesResponse
}

class StoriesApi(client: OkHttpClient) {
    private val retrofit = Retrofit.Builder()
        .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
        .client(client)
        .baseUrl(BuildConfig.BASE_URL)
        .build()

    val retrofitService: StoryApiService by lazy {
        retrofit.create(StoryApiService::class.java)
    }
}