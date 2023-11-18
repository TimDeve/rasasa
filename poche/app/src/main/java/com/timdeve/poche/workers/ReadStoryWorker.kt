package com.timdeve.poche.workers

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.franmontiel.persistentcookiejar.ClearableCookieJar
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.timdeve.poche.network.StoriesApi
import com.timdeve.poche.network.UpdateStoryRequest
import com.timdeve.poche.network.isOfflineException
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

private const val STORY_ID_PARAM = "article-id-param"

class ReadStoryWorker(ctx: Context, private val params: WorkerParameters) :
    CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val cookieJar: ClearableCookieJar =
            PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(applicationContext))

        val httpClient: OkHttpClient = OkHttpClient.Builder()
            .callTimeout(20.seconds.toJavaDuration())
            .connectTimeout(5.seconds.toJavaDuration())
            .cookieJar(cookieJar)
            .build()

        val storyApi = StoriesApi(httpClient)

        val storyId = params.inputData.getLong(STORY_ID_PARAM, -1)

        if (storyId < 0) {
            Log.e(this::class.simpleName, "Passed in storyId is negative")
            return Result.failure()
        }

        try {
            storyApi.retrofitService.updateStory(storyId, UpdateStoryRequest(isRead = true))
        } catch (e: Exception) {
            if (isOfflineException(e)) {
                return Result.retry()
            }
            Log.e(this::class.simpleName, "Failed to mark story as read: $e")
            return Result.failure()
        }

        return Result.success()
    }

    companion object {
        fun queue(ctx: Context, storyId: Long) {
            val constraints =
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

            val progressData = workDataOf(STORY_ID_PARAM to storyId)

            val request: OneTimeWorkRequest =
                OneTimeWorkRequestBuilder<ReadStoryWorker>()
                    .setConstraints(constraints)
                    .setInputData(progressData)
                    .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 20, TimeUnit.SECONDS)
                    .build()

            WorkManager.getInstance(ctx).enqueue(request)
        }

    }
}