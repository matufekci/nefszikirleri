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
        const val NOTIFICATION_ID_PACE = 7777

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

            // 1. Tempo matematiği: 1.140.000 zikir / 6 ay hedefi.
            //    Bant her gün çekilen zikirlere göre yeniden hesaplanır; ayar gerektirmez.
            val band = evaluatePaceBand(context, db)
            if (band != null) {
                sendPaceNotification(context, lang, strings, band)
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
                    Log.e(TAG, "DailyEvaluationWorker exceeded max retry limit ($MAX_RETRIES). Failing.")
                    return Result.failure()
                }
            } else {
                Log.e(TAG, "Permanent or unrecoverable error in DailyEvaluationWorker. Failing immediately.", e)
                return Result.failure()
            }
        }
    }

    /**
     * Tempo bandı değerlendirmesi.
     *
     * - Kalan zikir = Σ max(0, target − count) (tur ilerledikçe azalır)
     * - Tur başlangıcı = zikirlerdeki en erken startedAt; ilk [GRACE_DAYS] gün bildirim yok
     * - needDaily = kalan / (182 − geçen gün), [3.124, 5.000] bandına kıstırılır
     * - avg7 = son 7 TAM günün ortalaması (boş günler 0 sayılır — katı hesap)
     * - ON_TRACK iken haftada en fazla 1 teşvik gönderilir; aktif kullanıcıyı boğmaz
     *
     * @return gönderim yapılacak bant; null ise bugün bildirim gönderilmez
     */
    private suspend fun evaluatePaceBand(context: Context, db: AppDatabase): AdaptiveReminderManager.PaceBand? {
        if (!AdaptiveReminderManager.canSendNotificationToday(context)) {
            return null
        }

        val zikirs = db.zikirDao().getAllZikirsDirect()
        if (zikirs.isEmpty()) return null

        val remaining = zikirs.sumOf { (it.target - it.count).coerceAtLeast(0L) }
        if (remaining <= 0L) return null // tur bitmiş; yeni tur sıfırlanınca tekrar başlar

        val roundStart = zikirs.mapNotNull { it.startedAt }.filter { it > 0L }.minOrNull()
            ?: return null // kullanıcı henüz hiç başlamadı
        val elapsedDays = ((System.currentTimeMillis() - roundStart) / (24L * 60 * 60 * 1000L)).toInt()
        if (elapsedDays < AdaptiveReminderManager.GRACE_DAYS) return null

        val needDaily = AdaptiveReminderManager.computeNeedDaily(remaining, elapsedDays)

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calFrom = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -8) }
        // Toplu/otomatik sıçrama kayıtları (>10.000) bilinçli tempoya dahil edilmez
        val manualHistory = db.historyDao().getRecentManualHistoryDirect(calFrom.timeInMillis, 10000L)

        var weekSum = 0L
        for (i in 1..7) {
            val dayCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
            val dayKey = sdf.format(dayCal.time)
            weekSum += manualHistory.filter { it.dateKey == dayKey }.sumOf { it.amount }
        }
        val avg7 = weekSum / 7.0

        val band = AdaptiveReminderManager.paceBand(avg7, needDaily)

        // Tempo yerindeyken kullanıcı çok az bildirim alır: haftada en fazla 1 teşvik.
        if (band == AdaptiveReminderManager.PaceBand.ON_TRACK &&
            AdaptiveReminderManager.getWeeklySentCount(context) >= 1
        ) {
            return null
        }

        Log.d(TAG, "Pace eval: remaining=$remaining elapsed=$elapsedDays need=$needDaily avg7=$avg7 band=$band")
        return band
    }

    private fun sendPaceNotification(
        context: Context,
        lang: String,
        strings: UiTranslations,
        band: AdaptiveReminderManager.PaceBand
    ) {
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

        val reservation = AdaptiveReminderManager.tryReserveQuota(
            context,
            AdaptiveReminderManager.weeklyQuotaFor(band)
        ) ?: return

        // Tempo yerindeyse müjde; gerideyse uyarı ağırlıklı ayet gider.
        val verse = when (band) {
            AdaptiveReminderManager.PaceBand.ON_TRACK,
            AdaptiveReminderManager.PaceBand.MILD -> AdaptiveReminderManager.getRandomGladTidings(context)
            AdaptiveReminderManager.PaceBand.BEHIND -> AdaptiveReminderManager.getRandomWarning(context)
            AdaptiveReminderManager.PaceBand.CRITICAL -> AdaptiveReminderManager.getInactivityVerse(context)
        }
        val title = strings.adaptiveReminderNotifTitle
        val content = "${verse.surah}\n\"${verse.verseText}\""

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_PACE,
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
            notificationManager.notify(NOTIFICATION_ID_PACE, notification)
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
