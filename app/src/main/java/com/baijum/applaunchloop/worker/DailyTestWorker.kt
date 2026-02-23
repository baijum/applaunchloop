package com.baijum.applaunchloop.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.baijum.applaunchloop.R
import com.baijum.applaunchloop.data.repository.campaignDataStore
import com.baijum.applaunchloop.data.repository.CampaignRepositoryImpl
import kotlinx.coroutines.flow.first
import java.util.Calendar

class DailyTestWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val CHANNEL_ID = "daily_test_channel"
        const val NOTIFICATION_ID = 1001
        const val WORK_NAME = "daily_test_work"

        fun createNotificationChannel(context: Context) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Daily Test Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminds you to open your test app daily"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override suspend fun doWork(): Result {
        val repository = CampaignRepositoryImpl(context.campaignDataStore)

        val streak = repository.currentStreakCount.first()
        if (streak >= 14) return Result.success()

        val campaignId = repository.activeCampaignId.first()
        if (campaignId.isBlank()) return Result.success()

        val lastRun = repository.lastRunTimestamp.first()
        if (isSameDay(lastRun, System.currentTimeMillis())) return Result.success()

        val packageName = repository.targetPackageNames.first().firstOrNull()
            ?: return Result.success()

        showNotification(packageName)
        return Result.success()
    }

    private fun showNotification(targetPackageName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val launchIntent = context.packageManager.getLaunchIntentForPackage(targetPackageName)
        val pendingIntent = if (launchIntent != null) {
            PendingIntent.getActivity(
                context, 0, launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            null
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Time for your daily test run")
            .setContentText("Tap to open and test the app.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .apply { pendingIntent?.let { setContentIntent(it) } }
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    private fun isSameDay(timestamp1: Long, timestamp2: Long): Boolean {
        if (timestamp1 == 0L) return false
        val cal1 = Calendar.getInstance().apply { timeInMillis = timestamp1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = timestamp2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}
