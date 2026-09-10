package com.example.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteCantOpenDatabaseException
import android.database.sqlite.SQLiteDatabaseLockedException
import android.database.sqlite.SQLiteDiskIOException
import android.database.sqlite.SQLiteTableLockedException
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.model.AppStrings
import com.example.data.model.UiTranslations
import com.example.util.AdaptiveReminderManager
import com.example.util.NumberFormatter
import kotlinx.coroutines.CancellationException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DailyEvaluationWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "DailyEvaluationWorker"
        const val MAX_RETRIES = 3

        fun isTransientFailure(e: Throwable): Boolean {
            var current: Throwable? = e
            while (current != null) {
                if (current is IOException ||
                    current is SQLiteDatabaseLockedException ||
                    current is SQLiteTableLockedException ||
                    current is SQLiteDiskIOException ||
                    current is SQLiteCantOpenDatabaseException
                ) {
                    return true
                }
                current = current.cause
            }
            return false
        }
    }

    override suspend fun doWork(): Result {
        val context = applicationContext
        try {
            val db = AppDatabase.getDatabase(context)
            val settings = db.settingsDao().getSettingsDirect()
            val lang = settings?.lang ?: AdaptiveReminderManager.getAppLanguage(context)
            val strings = AppStrings.get(lang)

            // 0. Cleanup old pending operations (older than 7 days)
            try {
                val weekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
                db.pendingOperationDao().cleanupOldApplied(weekAgo)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.w(TAG, "Failed to cleanup old pending operations", e)
            }

            // 0b. Cleanup temporary backup files
            try {
                com.example.data.backup.BackupManager(context).cleanupTemporaryBackups()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.w(TAG, "Failed to cleanup temp backups", e)
            }

            // 1. Günlük zikir istatistiklerini ve adaptif hatırlatıcı zamanlarını değerlendir
            val shouldSend = evaluateAdaptiveReminder(context, db)
            if (shouldSend) {
                sendAdaptiveNotification(context, lang, strings)
            }

            // 2. Streak kontrolü yap (Optional logging or validation)
            checkStreakStatus(context, db)

            // 3. Process any unapplied pending operations (in case app was killed before batch)
            try {
                val repo = com.example.data.repository.ZikirRepository(db)
                repo.processUnappliedOperations()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.w(TAG, "Failed to process unapplied ops in worker", e)
            }

            return Result.success()
        } catch (e: CancellationException) {
            Log.d(TAG, "DailyEvaluationWorker execution was cancelled")
            throw e
        } catch (e: Throwable) {
            if (isTransientFailure(e)) {
                if (runAttemptCount < MAX_RETRIES) {
                    Log.w(TAG, "Transient failure encountered in DailyEvaluationWorker (attempt: $runAttemptCount). Retrying...", e)
                    return Result.retry()
                } else {
                    Log.e(TAG, "DailyEvaluationWorker exceeded max retry limit ($MAX_RETRIES). Failing.", e)
                    return Result.failure()
                }
            } else {
                Log.e(TAG, "Permanent or unrecoverable error in DailyEvaluationWorker. Failing immediately.", e)
                return Result.failure()
            }
        }
    }

    private suspend fun evaluateAdaptiveReminder(context: Context, db: AppDatabase): Boolean {
        if (!AdaptiveReminderManager.canSendNotificationToday(context)) {
            return false
        }

        val calFrom = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -8) }
        val manualHistory = db.historyDao().getRecentManualHistoryDirect(calFrom.timeInMillis, 10000L)
        if (manualHistory.isEmpty()) {
            return false
        }

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        val todayKey = sdf.format(cal.time)

        val todayAmount = manualHistory
            .filter { it.dateKey == todayKey }
            .sumOf { it.amount }

        val previousDaysKeys = mutableListOf<String>()
        for (i in 1..7) {
            val tempCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
            previousDaysKeys.add(sdf.format(tempCal.time))
        }

        val pastDaysWithActivity = mutableMapOf<String, Long>()
        for (dayKey in previousDaysKeys) {
            val daySum = manualHistory
                .filter { it.dateKey == dayKey }
                .sumOf { it.amount }
            if (daySum > 0) {
                pastDaysWithActivity[dayKey] = daySum
            }
        }

        if (pastDaysWithActivity.size < 2) {
            return false
        }

        val pastAverage = pastDaysWithActivity.values.average()
        if (pastAverage < 50) {
            return false
        }

        val isSignificantlyReduced = todayAmount < (pastAverage * 0.5)
        return isSignificantlyReduced
    }

    private fun sendAdaptiveNotification(context: Context, lang: String, strings: UiTranslations) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (!hasPermission) {
                return
            }
        }

        val channelId = "dhikr_reminders_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                strings.title,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = strings.subtitle
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val reservation = AdaptiveReminderManager.tryReserveQuota(context) ?: return

        val verse = AdaptiveReminderManager.getRandomSpiritualVerse(lang)
        val title = strings.adaptiveReminderNotifTitle
        val content = "${verse.surah}\n\"${verse.verseText}\""
        val notificationId = 7777

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            notificationManager.notify(notificationId, notification)
        } catch (e: Exception) {
            AdaptiveReminderManager.rollbackQuotaReservation(context, reservation)
            throw e
        }
    }

    private suspend fun checkStreakStatus(context: Context, db: AppDatabase) {
        try {
            val activeDates = db.historyDao().getDistinctActiveDatesDirect()
            val activeDays = activeDates.toSet()
            var streakCount = 0
            val cal = Calendar.getInstance()
            while (activeDays.contains(NumberFormatter.getDateKey(cal.time))) {
                streakCount++
                cal.add(Calendar.DAY_OF_YEAR, -1)
            }
            Log.d(TAG, "Current streak verification in background: $streakCount days")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to verify streak status during background evaluation", e)
            if (isTransientFailure(e)) {
                throw e
            }
        }
    }
}
