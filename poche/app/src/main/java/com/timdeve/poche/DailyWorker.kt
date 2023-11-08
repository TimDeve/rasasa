package com.timdeve.poche

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.franmontiel.persistentcookiejar.ClearableCookieJar
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.timdeve.poche.network.ArticleApi
import com.timdeve.poche.network.StoriesApi
import com.timdeve.poche.persistence.PocheDatabase
import com.timdeve.poche.repository.ArticlesRepository
import com.timdeve.poche.repository.StoriesRepository
import okhttp3.OkHttpClient
import java.util.Calendar
import java.util.concurrent.TimeUnit

const val TAG = "daily-worker"

fun scheduleNextWorker(ctx: Context) {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.UNMETERED)
        .setRequiresCharging(true)
        .build()

    val dailyWorkRequest = OneTimeWorkRequestBuilder<DailyWorker>()
        .setConstraints(constraints)
        .setInitialDelay(diffToTargetTime(), TimeUnit.MILLISECONDS)
        .addTag(TAG)
        .build()

    WorkManager.getInstance(ctx)
        .enqueueUniqueWork(TAG, ExistingWorkPolicy.REPLACE, dailyWorkRequest)
}

private fun diffToTargetTime(): Long {
    val currentDate = Calendar.getInstance()
    val dueDate = Calendar.getInstance()
    dueDate.set(Calendar.HOUR_OF_DAY, 7)
    dueDate.set(Calendar.MINUTE, 30)
    dueDate.set(Calendar.SECOND, 0)

    if (dueDate.before(currentDate)) {
        dueDate.add(Calendar.HOUR_OF_DAY, 24)
    }

    return dueDate.timeInMillis - currentDate.timeInMillis
}

class DailyWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        val cookieJar: ClearableCookieJar =
            PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(applicationContext))

        val httpClient: OkHttpClient = OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .build()

        val db = PocheDatabase.make(applicationContext)

        val storyApi = StoriesApi(httpClient)
        val storiesRepository = StoriesRepository(db.storiesDao(), storyApi)

        val articleApi = ArticleApi(httpClient)
        val articlesRepository = ArticlesRepository(db.articlesDao(), articleApi)

        val stories = storiesRepository.getStories()
        for (story in stories) {
            articlesRepository.fetchArticle(story.url)
        }

        scheduleNextWorker(applicationContext)
        return Result.success()
    }
}