package com.timdeve.poche.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
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
import com.timdeve.poche.R
import com.timdeve.poche.network.ArticleApi
import com.timdeve.poche.network.FeedsApi
import com.timdeve.poche.network.StoriesApi
import com.timdeve.poche.persistence.PocheDatabase
import com.timdeve.poche.repository.ArticlesRepository
import com.timdeve.poche.repository.FeedsRepository
import com.timdeve.poche.repository.Repositories
import com.timdeve.poche.repository.StoriesRepository
import kotlinx.coroutines.CancellationException
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

// Somewhat blunt workaround for websites that rates limit
private val dontCacheThoseHosts = setOf(
    "www.nytimes.com",
)

class CacheWorker(private val ctx: Context, params: WorkerParameters) :
    CoroutineWorker(ctx, params) {
    init {
        createNotificationChannel()
    }

    override suspend fun doWork(): Result {
        var cancellationException: Exception? = null

        try {
            cacheStoriesAndArticle()
        } catch (e: Exception) {
            if (e is CancellationException) {
                cancellationException = e
            } else {
                createNotification(
                    notificationTitle = "Uncaught Exception while Caching",
                    notificationBody = e.toString(),
                    clearPrevious = true,
                )
                Log.e(this::class.simpleName, "Uncaught Exception when caching: $e")
            }
        }

        try {
            if (!isStopped) {
                clearOldCachedData()
            }
        } catch (e: Exception) {
            if (e is CancellationException) {
                cancellationException = e
            } else {
                createNotification(
                    notificationTitle = "Uncaught Exception while cleaning up",
                    notificationBody = e.toString(),
                    clearPrevious = true,
                )
                Log.e(this::class.simpleName, "Uncaught Exception when cleaning up: $e")
            }
        }

        if (isStopped || cancellationException != null) clearNotification()

        schedule(applicationContext)

        cancellationException?.let {
            throw cancellationException
        }

        return Result.success()
    }

    private suspend fun cacheStoriesAndArticle() {
        val repos = makeRepos()

        var errors = 0
        val stories = repos.stories.getStories()
        stories.forEachIndexed { i, story ->
            if (isStopped) return

            if (!dontCacheThoseHosts.contains(story.url.host)) {
                try {
                    val article = repos.articles.getArticle(story.url)
                    if (article == null) {
                        createNotification(story.title, progressTarget = stories.size, progress = i)
                        repos.articles.fetchArticle(story.url)
                    }
                } catch (e: Exception) {
                    errors++
                    Log.e(this::class.simpleName, "Exception when fetching article: $e")
                }
            }
        }

        createNotification(
            if (errors > 0) "Finished with $errors errors" else "Done",
            notificationTitle = "Cached Stories",
            clearPrevious = true,
        )
    }

    private suspend fun clearOldCachedData() {
        val repos = makeRepos()

        repos.stories.deleteOldStories()
        repos.articles.deleteArticlesWithoutStories()
    }

    private fun makeRepos(): Repositories {
        val cookieJar: ClearableCookieJar =
            PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(applicationContext))

        val httpClient: OkHttpClient = OkHttpClient.Builder()
            .callTimeout(20.seconds.toJavaDuration())
            .connectTimeout(5.seconds.toJavaDuration())
            .cookieJar(cookieJar)
            .build()

        val db = PocheDatabase.make(applicationContext)

        val articleApi = ArticleApi(httpClient)
        val articlesRepository = ArticlesRepository(db.articlesDao(), articleApi)

        val feedsApi = FeedsApi(httpClient)
        val feedsRepository = FeedsRepository(db.feedListsDao(), feedsApi)

        val storyApi = StoriesApi(httpClient)
        val storiesRepository = StoriesRepository(ctx, db.storiesDao(), storyApi)

        return Repositories(
            articlesRepository,
            feedsRepository,
            storiesRepository,
        )
    }

    private fun createNotificationChannel() {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val mChannel =
            NotificationChannel(CHANNEL_ID, "Cache Stories", NotificationManager.IMPORTANCE_DEFAULT)

        mChannel.description = "Caches Stories"

        notificationManager.createNotificationChannel(mChannel)
    }

    private fun clearNotification() {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
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
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

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