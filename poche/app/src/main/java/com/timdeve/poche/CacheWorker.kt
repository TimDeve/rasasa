package com.timdeve.poche

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.util.Log
import androidx.core.app.NotificationCompat
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
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

private const val TAG = "cache-request"
private const val WORKER_NAME = "$TAG-worker-name"
private const val CHANNEL_ID = "$TAG-channel-id"

// Static NOTIFICATION_ID so that new notifications take over older ones
private const val NOTIFICATION_ID = 0x90c4e

class CacheWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    init {
        createNotificationChannel()
    }

    override suspend fun doWork(): Result {
        try {
            cacheStoriesAndArticle()
        } catch (e: Exception) {
            createNotification(
                notificationTitle = "Uncaught Exception while Caching",
                notificationBody = e.toString(),
                clearPrevious = true,
            )
            Log.e("CacheWorker", "Uncaught Exception when caching: $e")
        }

        schedule(applicationContext)
        return Result.success()
    }

    private suspend fun cacheStoriesAndArticle() {
        val cookieJar: ClearableCookieJar =
            PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(applicationContext))

        val httpClient: OkHttpClient = OkHttpClient.Builder()
            .callTimeout(20.seconds.toJavaDuration())
            .connectTimeout(5.seconds.toJavaDuration())
            .cookieJar(cookieJar)
            .build()

        val db = PocheDatabase.make(applicationContext)

        val storyApi = StoriesApi(httpClient)
        val storiesRepository = StoriesRepository(db.storiesDao(), storyApi)

        val articleApi = ArticleApi(httpClient)
        val articlesRepository = ArticlesRepository(db.articlesDao(), articleApi)

        var errors = 0
        val stories = storiesRepository.getStories()
        stories.forEachIndexed { i, story ->
            if (isStopped) {
                clearNotification()
                return
            }
            try {
                val article = articlesRepository.getArticle(story.url)
                if (article == null) {
                    createNotification(story.title, progressTarget = stories.size, progress = i)
                    articlesRepository.fetchArticle(story.url)
                }
            } catch (e: Exception) {
                errors++
                Log.e("CacheWorker", "Exception when fetching article: $e")
            }
        }

        createNotification(
            if (errors > 0) "Finished with $errors errors" else "Done",
            notificationTitle = "Cached Stories",
            clearPrevious = true,
        )
    }

    private fun createNotificationChannel() {
        val notificationManager =
            applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val mChannel =
            NotificationChannel(CHANNEL_ID, "Cache Stories", NotificationManager.IMPORTANCE_DEFAULT)

        mChannel.description = "Caches Stories"

        notificationManager.createNotificationChannel(mChannel)
    }

    private fun clearNotification() {
        val notificationManager =
            applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }

    private fun createNotification(
        notificationBody: String,
        notificationTitle: String = "Caching Stories...",
        progressTarget: Int = 0,
        progress: Int = 0,
        clearPrevious: Boolean = false
    ) {
        val notificationManager =
            applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (clearPrevious) {
            clearNotification()
        }

        val notification = NotificationCompat.Builder(
            applicationContext,
            CHANNEL_ID
        )
            .setContentTitle(notificationTitle)
            .setContentText(notificationBody)
            .setSmallIcon(R.drawable.notification_icon)
            .setSilent(true)

        if (progressTarget > 0) {
            val cancelText = "Cancel"
            val cancelPendingIntent =
                WorkManager.getInstance(applicationContext).createCancelPendingIntent(id)

            val action = NotificationCompat.Action.Builder(
                R.drawable.notification_icon,
                cancelText,
                cancelPendingIntent
            ).build()

            notification.addAction(action)
                .setProgress(progressTarget, progress, false)
                .setOngoing(true)
        }

        notificationManager.notify(NOTIFICATION_ID, notification.build())
    }

    companion object {

        fun schedule(ctx: Context, delay: Long = diffToTargetTime(), constrain: Boolean = true) {

            val dailyWorkRequest = OneTimeWorkRequestBuilder<CacheWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .addTag(TAG)

            if (constrain) {
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.UNMETERED)
                    .setRequiresCharging(true)
                    .build()

                dailyWorkRequest.setConstraints(constraints)
            }

            WorkManager.getInstance(ctx)
                .enqueueUniqueWork(
                    WORKER_NAME,
                    ExistingWorkPolicy.REPLACE,
                    dailyWorkRequest.build()
                )
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
    }
}