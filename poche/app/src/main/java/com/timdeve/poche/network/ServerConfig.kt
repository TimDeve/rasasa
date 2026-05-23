package com.timdeve.poche.network

import android.content.Context
import com.timdeve.poche.BuildConfig

class ServerConfig(context: Context) {
    private val prefs = context.getSharedPreferences("poche_prefs", Context.MODE_PRIVATE)

    var url: String
        get() = prefs.getString("server_url", BuildConfig.BASE_URL) ?: BuildConfig.BASE_URL
        set(value) {
            prefs.edit().putString("server_url", value).apply()
        }
}
