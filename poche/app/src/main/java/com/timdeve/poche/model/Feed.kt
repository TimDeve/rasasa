package com.timdeve.poche.model

import kotlinx.serialization.Serializable

@Serializable
data class Feed(
    val id: Long,
    val name: String,
    val url: String,
)

@Serializable
data class FeedList(
    val id: Long,
    val name: String,
    val feedIds: List<Long>,
)

// genFeeds generates fake data for previews
fun genFeeds(): Pair<Map<Long, Feed>, Map<Long, FeedList>> {
    return Pair(
        listOf(
            0L to Feed(0L, "Hacker News", "http://example.com"),
            1L to Feed(1L, "News Org", "http://example.com"),
            2L to Feed(2L, "Tech Knowledge", "http://example.com"),
            3L to Feed(3L, "Stuff N' Things", "http://example.com"),
        ).toMap(),
        listOf(
            0L to FeedList(0L, "My List", listOf(0, 3)),
            1L to FeedList(1L, "Another one", listOf(0, 3)),
        ).toMap()
    )
}
