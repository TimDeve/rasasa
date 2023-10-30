package com.timdeve.poche.model

import kotlinx.serialization.Serializable

@Serializable
data class Story (
    val id: Int,
    val feedId: Int,
    val title: String,
    val url: String,
    val isRead: Boolean,
    val publishedDate: String,
    val content: String,
)