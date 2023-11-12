package com.timdeve.poche

import java.net.URL
import java.net.URLEncoder

object PocheDestinations {
    const val HOME_ROUTE = "home"
    const val LOGIN_ROUTE = "login"
    const val ARTICLE_ROUTE = "article"
    const val LISTS_ROUTE = "feed-lists"
    const val STORIES_ROUTE = "stories"
}

object PocheNavigate {
    fun article(url: URL): String {
        val encodedUrl = URLEncoder.encode(url.toString(), "UTF-8")
        return "${PocheDestinations.ARTICLE_ROUTE}/$encodedUrl"
    }

    fun stories(listId: Long): String {
        return "${PocheDestinations.STORIES_ROUTE}/$listId"
    }
}

