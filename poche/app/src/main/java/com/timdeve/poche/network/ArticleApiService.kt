package com.timdeve.poche.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.timdeve.poche.BuildConfig
import com.timdeve.poche.model.Article
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query
import java.net.URL


interface ArticleApiService {
    @GET("api/v0/read")
    suspend fun getArticle(@Query("page") url: URL, @Query("format") format: String): Article
}

class ArticleApi(client: OkHttpClient): ArticleApiService {
    private val retrofit = Retrofit.Builder()
        .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
        .client(client)
        .baseUrl(BuildConfig.BASE_URL)
        .build()

    private val retrofitService: ArticleApiService by lazy {
        retrofit.create(ArticleApiService::class.java)
    }

    override suspend fun getArticle(url: URL, format: String): Article {
        return retrofitService.getArticle(url, format)
    }
}
