package com.timdeve.poche.network

import com.timdeve.poche.BuildConfig
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.timdeve.poche.model.Feed
import com.timdeve.poche.model.FeedList
import com.timdeve.poche.model.Story
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.http.GET

@Serializable
data class GetFeedListsResponse(
    val lists: List<FeedList>
)

@Serializable
data class GetFeedsResponse(
    val feeds: List<Feed>
)

interface FeedsApiService {
    @GET("api/v0/lists")
    suspend fun getFeedLists(): GetFeedListsResponse
    @GET("api/v0/feeds")
    suspend fun getFeeds(): GetFeedsResponse
}

class FeedsApi(client: OkHttpClient): FeedsApiService {
    private val retrofit = Retrofit.Builder()
        .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
        .client(client)
        .baseUrl(BuildConfig.BASE_URL)
        .build()

    private val retrofitService: FeedsApiService by lazy {
        retrofit.create(FeedsApiService::class.java)
    }

    override suspend fun getFeedLists(): GetFeedListsResponse {
        return retrofitService.getFeedLists()
    }

    override suspend fun getFeeds(): GetFeedsResponse {
        return retrofitService.getFeeds()
    }
}
