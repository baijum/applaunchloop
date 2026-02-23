package com.baijum.applaunchloop.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object WorkScheduler {

    fun scheduleDailyTestWork(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<DailyTestWorker>(
            repeatInterval = 24,
            repeatIntervalTimeUnit = TimeUnit.HOURS,
            flexTimeInterval = 1,
            flexTimeIntervalUnit = TimeUnit.HOURS
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            DailyTestWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    fun cancelDailyTestWork(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(DailyTestWorker.WORK_NAME)
    }
}
