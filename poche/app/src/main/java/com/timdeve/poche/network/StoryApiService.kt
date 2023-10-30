package com.timdeve.poche.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.timdeve.poche.model.Story
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.http.GET

private const val BASE_URL =
    "http://10.0.2.2:8091"

/**
 * Use the Retrofit builder to build a retrofit object using a kotlinx.serialization converter
 */
private val retrofit = Retrofit.Builder()
    .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
    .baseUrl(BASE_URL)
    .build()

@Serializable
data class GetStoriesResponse(
    val stories: List<Story>
)

interface StoryApiService {
    @GET("v0/stories")
    suspend fun getStories(): GetStoriesResponse
}

object StoriesApi {
    val retrofitService: StoryApiService by lazy {
        retrofit.create(StoryApiService::class.java)
    }
}
