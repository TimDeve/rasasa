package com.timdeve.poche.repository

data class Repositories(
    val articles: ArticlesRepository,
    val feeds: FeedsRepository,
    val stories: StoriesRepository
)