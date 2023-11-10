package com.timdeve.poche

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
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

const val TAG = "daily-worker"
const val CHANNEL_ID = "$TAG-channel-id"

class CacheWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    private val notificationId = 1337

    init {
        createNotificationChannel()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override suspend fun doWork(): Result {
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
            createNotification(i, stories.size, story.title)
            try {
                articlesRepository.fetchArticle(story.url)
            } catch (e: Exception) {
                errors++
                Log.e("CacheWorker", "Exception when fetching article: $e")
            }
        }

        createNotification(
            stories.size + 1,
            stories.size,
            if (errors > 0) "Finished with $errors errors" else "Done",
        )

        schedule(applicationContext)
        return Result.success()
    }

    private fun createNotificationChannel() {
        val notificationManager =
            applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val mChannel =
            NotificationChannel(CHANNEL_ID, "Cache Stories", NotificationManager.IMPORTANCE_DEFAULT)

        mChannel.description = "Caches Stories"

        notificationManager.createNotificationChannel(mChannel)
    }

    private fun createNotification(downloadProgress: Int, storiesSize: Int, storyTitle: String) {
        val notificationManager =
            applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val cancelText = "Cancel"
        val cancelPendingIntent =
            WorkManager.getInstance(applicationContext).createCancelPendingIntent(id)

        val action = NotificationCompat.Action.Builder(
            R.drawable.notification_icon,
            cancelText,
            cancelPendingIntent
        ).build()

        val notification = NotificationCompat.Builder(
            applicationContext,
            CHANNEL_ID
        )
            .setContentText(storyTitle)
            .setSmallIcon(R.drawable.notification_icon)
            .setSilent(true)

        if (downloadProgress <= storiesSize) {
            notification.addAction(action)
                .setContentTitle("Caching Stories...")
                .setProgress(storiesSize, downloadProgress, false)
                .setOngoing(true)
        } else {
            notification.setContentTitle("Cached Stories")
        }

        notificationManager.notify(notificationId, notification.build())
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
                .enqueueUniqueWork(TAG, ExistingWorkPolicy.REPLACE, dailyWorkRequest.build())
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