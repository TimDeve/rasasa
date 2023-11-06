package com.timdeve.poche.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Story(
    val id: Long,
    val feedId: Long,
    val title: String,
    val url: String,
    var isRead: Boolean,
    val publishedDate: Instant,
    val content: String,
)

private val storyTitles = listOf(
    "Something is happening",
    "In a world where apps don't exist, a developer will go to great length to find something to make",
    "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore",
    "The world is coming to an end!",
    "The actual teachings of the great explorer of the truth, the master-builder of human happiness"
)

// genStories generates fake data for previews
fun genStories(): List<Story> {
    return LongRange(0, 200).map {
        Story(
            id = it,
            feedId = (it * it + 1) % 5,
            title = storyTitles[(it % storyTitles.size).toInt()],
            url = "http://example.com",
            isRead = it < 3,
            publishedDate = Instant.DISTANT_PAST,
            content = "",
        )
    }
}