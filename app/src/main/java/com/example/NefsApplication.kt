package com.example

import android.app.Application
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.worker.DailyEvaluationWorker
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class NefsApplication : Application(), Configuration.Provider {

    companion object {
        val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        fun scheduleDailyEvaluation(context: android.content.Context) {
            try {
                val constraints = Constraints.Builder()
                    .setRequiresBatteryNotLow(false)
                    .setRequiresDeviceIdle(false)
                    .build()

                // Align to next 08:00 to have consistent daily evaluation time
                val now = java.util.Calendar.getInstance()
                val nextRun = java.util.Calendar.getInstance().apply {
                    set(java.util.Calendar.HOUR_OF_DAY, 8)
                    set(java.util.Calendar.MINUTE, 0)
                    set(java.util.Calendar.SECOND, 0)
                    set(java.util.Calendar.MILLISECOND, 0)
                    if (before(now)) add(java.util.Calendar.DAY_OF_YEAR, 1)
                }
                val initialDelay = nextRun.timeInMillis - now.timeInMillis

                val periodicWorkRequest = PeriodicWorkRequestBuilder<DailyEvaluationWorker>(1, TimeUnit.DAYS)
                    .setConstraints(constraints)
                    .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                    .build()

                WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    "DailyEvaluationWork",
                    ExistingPeriodicWorkPolicy.UPDATE,
                    periodicWorkRequest
                )
            } catch (e: Exception) {
                if (com.example.BuildConfig.DEBUG) {
                    android.util.Log.e("NefsApplication", "Failed to schedule DailyEvaluationWorker", e)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        scheduleDailyEvaluation(this)
    }
    
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    private fun setupWorkManager() {
        scheduleDailyEvaluation(this)
    }
}
