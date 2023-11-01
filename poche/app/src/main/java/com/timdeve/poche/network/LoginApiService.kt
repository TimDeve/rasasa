package com.timdeve.poche.network

import com.timdeve.poche.BuildConfig
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.POST

@Serializable
data class LoginRequest(val username: String, val password: String)

interface LoginApiService {
    @POST("api/v0/login")
    suspend fun login(@Body body: LoginRequest)
}

class LoginApi(client: OkHttpClient) {
    private val retrofit = Retrofit.Builder()
        .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
        .client(client)
        .baseUrl(BuildConfig.BASE_URL)
        .build()

    val retrofitService: LoginApiService by lazy {
        retrofit.create(LoginApiService::class.java)
    }
}
