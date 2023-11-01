package com.timdeve.poche.model

import kotlinx.serialization.Serializable

@Serializable
data class Story(
    val id: Int,
    val feedId: Int,
    val title: String,
    val url: String,
    val isRead: Boolean,
    val publishedDate: String,
    val content: String,
)

private val storyTitles = listOf(
    "Something is happening",
    "In a world where apps don't exist, a developer will go to great length to find something to make",
    "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore",
    "The world is coming to an end!",
    "The actual teachings of the great explorer of the truth, the master-builder of human happiness"
)

fun genRandomStories(): List<Story> {
    return IntRange(0, 200).map {
        Story(
            id = it,
            feedId = it % 3 * it,
            title = storyTitles[it % storyTitles.size],
            url = "http://example.com",
            isRead = false,
            publishedDate = "",
            content = "",
        )
    }
}