package com.timdeve.poche.network

import android.content.Context
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

suspend fun swallowOfflineExceptions(cb: suspend () -> Unit) {
    try {
        cb()
    } catch (e: Exception) {
        isOfflineException(e) || throw e
    }
}

fun isOfflineException(e: Exception): Boolean {
    return when (e) {
        is UnknownHostException -> true
        is SocketTimeoutException -> true
        is ConnectException -> true
        else -> false
    }
}

fun OkHttpClient.Builder.addDynamicBaseUrlInterceptor(context: Context): OkHttpClient.Builder {
    val serverConfig = ServerConfig(context)
    return this.addInterceptor { chain ->
        val request = chain.request()
        val urlToUse = serverConfig.url.toHttpUrlOrNull()
        if (urlToUse != null) {
            val newUrl = request.url.newBuilder()
                .scheme(urlToUse.scheme)
                .host(urlToUse.host)
                .port(urlToUse.port)
                .build()
            val newRequest = request.newBuilder().url(newUrl).build()
            chain.proceed(newRequest)
        } else {
            chain.proceed(request)
        }
    }
}