package com.timdeve.poche.repository

import com.timdeve.poche.network.FeedsApiService
import com.timdeve.poche.network.swallowOfflineExceptions
import com.timdeve.poche.persistence.Feed
import com.timdeve.poche.persistence.FeedList
import com.timdeve.poche.persistence.FeedListsDao
import com.timdeve.poche.persistence.fromModel
import kotlinx.coroutines.flow.Flow

class FeedsRepository(
    private val feedListsDao: FeedListsDao,
    private val feedsApiService: FeedsApiService,
) {
    fun getFeedLists(): Flow<List<FeedList>> {
        return feedListsDao.getFeedLists()
    }

    suspend fun fetchFeedLists() {
        swallowOfflineExceptions {
            val lists = feedsApiService.getFeedLists()
            feedListsDao.insertFeedLists(lists.lists.map { FeedList.fromModel(it) })
        }
    }

    fun getFeeds(): Flow<List<Feed>> {
        return feedListsDao.getFeeds()
    }

    suspend fun fetchFeeds() {
        swallowOfflineExceptions {
            val feeds = feedsApiService.getFeeds()
            feedListsDao.insertFeeds(feeds.feeds.map { Feed.fromModel(it) })
        }
    }
}

