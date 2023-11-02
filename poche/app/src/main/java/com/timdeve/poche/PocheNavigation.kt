package com.timdeve.poche

import java.net.URLEncoder

object PocheDestinations {
    const val HOME_ROUTE = "home"
    const val LOGIN_ROUTE = "login"
    const val ARTICLE_ROUTE = "article"
}

object PocheNavigate {
    fun article(url: String): String {
        val encodedUrl = URLEncoder.encode(url, "UTF-8")
        return "${PocheDestinations.ARTICLE_ROUTE}/$encodedUrl"
    }
}

