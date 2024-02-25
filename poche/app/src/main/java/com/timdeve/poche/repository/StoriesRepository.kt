package com.timdeve.poche.repository

import android.content.Context
import android.util.Log
import com.timdeve.poche.network.StoriesApi
import com.timdeve.poche.network.UpdateStoryRequest
import com.timdeve.poche.network.isOfflineException
import com.timdeve.poche.network.swallowOfflineExceptions
import com.timdeve.poche.persistence.StoriesDao
import com.timdeve.poche.persistence.Story
import com.timdeve.poche.persistence.fromModel
import com.timdeve.poche.workers.ReadStoryWorker
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.days

class StoriesRepository(
    private val ctx: Context,
    private val storiesDao: StoriesDao,
    private val storiesApi: StoriesApi
) {
    suspend fun getStories(
        listId: Long? = null,
        read: Boolean = false,
        cachedOnly: Boolean = false
    ): List<Story> {
        if (cachedOnly) {
            if (listId != null) {
                return storiesDao.getCachedStoriesByListId(listId, read)
            }
            return storiesDao.getCachedStories(read)
        }

        swallowOfflineExceptions {
            val res = storiesApi.retrofitService.getStories(read, listId)
            storiesDao.insertStories(res.stories.map { Story.fromModel(it) })
        }

        if (listId != null) {
            return storiesDao.getStoriesByListId(listId, read)
        }
        return storiesDao.getStories(read)
    }

    suspend fun markStoryAsRead(id: Long) {
        try {
            storiesApi.retrofitService.updateStory(id, UpdateStoryRequest(id, true))
        } catch (e: Exception) {
            if (isOfflineException(e)) {
                Log.d("offline?", id.toString())
                ReadStoryWorker.queue(ctx, listOf(id))
            } else {
                throw e
            }
        }

        storiesDao.markStoryAsRead(id)
    }

    suspend fun markStoriesAsRead(ids: List<Long>) {
        try {
            val requestBody = ids.map { UpdateStoryRequest(it, true) }
            storiesApi.retrofitService.updateStories(requestBody)
        } catch (e: Exception) {
            if (isOfflineException(e)) {
                Log.d("offline?", ids.toString())
                ReadStoryWorker.queue(ctx, ids)
            } else {
                throw e
            }
        }

        storiesDao.markStoriesAsRead(ids)
    }

    suspend fun deleteOldStories() {
        storiesDao.deleteStoriesOlderThan(Clock.System.now().minus(7.days))
    }
}