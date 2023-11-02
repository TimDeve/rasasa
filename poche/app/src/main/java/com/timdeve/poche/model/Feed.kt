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
