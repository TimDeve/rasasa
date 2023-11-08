package com.timdeve.poche.network

import java.net.SocketTimeoutException
import java.net.UnknownHostException

suspend fun swallowOfflineExceptions(cb: suspend () -> Unit) {
    try {
        cb()
    } catch (e: Exception) {
        when (e) {
            is UnknownHostException -> Unit
            is SocketTimeoutException -> Unit
            else -> throw e
        }
    }
}