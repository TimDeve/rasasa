package com.timdeve.poche.repository

import android.util.Log
import com.timdeve.poche.network.FeedsApiService
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
        val lists = feedsApiService.getFeedLists()
        Log.d("Wow", lists.toString())
        feedListsDao.insertFeedLists(lists.lists.map { FeedList.fromModel(it) })
    }
}

