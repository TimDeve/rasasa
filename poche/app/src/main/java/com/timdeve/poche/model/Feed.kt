package com.timdeve.poche.model

import kotlinx.serialization.Serializable

@Serializable
data class Feed(
    val id: Int,
    val name: String,
    val url: String,
)

@Serializable
data class FeedList(
    val id: Int,
    val name: String,
    val feedIds: List<Int>,
)

// genFeeds generates fake data for previews
fun genFeeds(): Pair<Map<Int, Feed>, Map<Int, FeedList>> {
    return Pair(
        listOf(
            0 to Feed(0, "Hacker News", "http://example.com"),
            1 to Feed(1, "News Org", "http://example.com"),
            2 to Feed(2, "Tech Knowledge", "http://example.com"),
            3 to Feed(3, "Stuff N' Things", "http://example.com"),
        ).toMap(),
        listOf(
            0 to FeedList(0, "My List", listOf(0, 3)),
            1 to FeedList(1, "Another one", listOf(0, 3)),
        ).toMap()
    )
}
