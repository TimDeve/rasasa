package com.timdeve.poche.model

import kotlinx.serialization.Serializable

@Serializable
data class Article(
    val readable: Boolean,
    val title: String = "",
    val byline: String? = "",
    val content: String = "",
    val url: String = "",
)