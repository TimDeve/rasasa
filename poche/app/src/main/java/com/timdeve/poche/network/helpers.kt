package com.timdeve.poche.network

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