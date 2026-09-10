package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.model.AppStrings
import com.example.util.AdaptiveReminderManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ReminderAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra(EXTRA_TYPE) ?: TYPE_DAILY_REMINDER
        val pendingResult = goAsync()
        
        com.example.NefsApplication.applicationScope.launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val settings = db.settingsDao().getSettingsDirect()
                val lang = settings?.lang ?: AdaptiveReminderManager.getAppLanguage(context)
                val strings = AppStrings.get(lang)

                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                    ?: return@launch

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.POST_NOTIFICATIONS
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                    if (!hasPermission) {
                        return@launch
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

                val title: String
                val content: String
                val notificationId: Int
                var quotaReservation: AdaptiveReminderManager.QuotaReservation? = null

                if (type == TYPE_ADAPTIVE_CHECK) {
                    // Akıllı Manevi Hatırlatıcı Analiz Motoru
                    val shouldSend = evaluateAdaptiveReminder(context, db)
                    if (shouldSend) {
                        val reservation = AdaptiveReminderManager.tryReserveQuota(context)
                        if (reservation == null) {
                            AdaptiveReminderManager.schedulePeriodicEvaluation(context)
                            return@launch
                        }
                        quotaReservation = reservation
                        val verse = AdaptiveReminderManager.getRandomSpiritualVerse(lang)
                        title = strings.adaptiveReminderNotifTitle
                        content = "${verse.surah}\n\"${verse.verseText}\""
                        notificationId = NOTIFICATION_ID_ADAPTIVE
                    } else {
                        // Bildirim şartları oluşmadı, sessizce sonraki günü planla ve çık
                        AdaptiveReminderManager.schedulePeriodicEvaluation(context)
                        return@launch
                    }
                } else if (type == TYPE_INACTIVITY) {
                    title = strings.inactivityNotifTitle
                    content = strings.inactivityNotifBody
                    notificationId = 9999
                } else if (type == TYPE_TARGET_REMINDER) {
                    if (settings?.targetReminderEnabled != true) {
                        return@launch
                    }
                    val todayKey = com.example.util.NumberFormatter.getDateKey()
                    val targetDaily = settings?.dailyTarget ?: 10000L
                    val selectedId = settings?.selectedZikirId ?: 1
                    val todayDone = db.historyDao().getTodayRecitedForZikirDirect(selectedId, todayKey)
                    val remainingToday = (targetDaily - todayDone).coerceAtLeast(0L)
                    if (remainingToday <= 0L) {
                        // Daily target already met, no need to remind
                        return@launch
                    }
                    title = strings.targetReminderTitle
                    content = "${strings.remainingZikir}: ${com.example.util.NumberFormatter.format(remainingToday, lang)}"
                    notificationId = 8888
                } else {
                    if (settings?.reminderEnabled != true) {
                        return@launch
                    }
                    val slotRequestCode = intent.getIntExtra(EXTRA_SLOT_ID, 1000)
                    val dbSlots = db.reminderDao().getAllSlotsList()
                    if (dbSlots.isEmpty()) {
                        // No slots configured, nothing to notify
                        return@launch
                    }
                    val isSlotActive = dbSlots.any { slot ->
                        slot.isEnabled && com.example.util.NotificationScheduler.getSlotRequestCode(slot.id) == slotRequestCode
                    }
                    if (!isSlotActive) {
                        // Silinmiş veya devre dışı bırakılmış slotun alarmı çalışmamalı
                        return@launch
                    }
                    title = strings.title
                    content = "${strings.todayVird} - ${strings.subtitle}"
                    notificationId = slotRequestCode
                }

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
                    quotaReservation?.let { AdaptiveReminderManager.rollbackQuotaReservation(context, it) }
                    throw e
                }

                // Herhangi bir akıllı kontrol tamamlandığında bir sonraki periyodu otomatik tazele
                if (type == TYPE_ADAPTIVE_CHECK) {
                    AdaptiveReminderManager.schedulePeriodicEvaluation(context)
                }

                // If inactivity alarm was triggered, schedule the next cycle if still enabled
                if (type == TYPE_INACTIVITY && settings?.inactivityAlertEnabled == true) {
                    com.example.util.NotificationScheduler(context).scheduleInactivityAlert(true)
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                if (com.example.BuildConfig.DEBUG) {
                    android.util.Log.e("ReminderAlarmReceiver", "Failed to dispatch reminder notification", e)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    /**
     * Kullanıcının zikir temposunu inceler:
     * - Toplu / otomatik sıçrama zikirlerini (>10,000 tek seferlik) göz ardı eder
     * - Önceki 7 günün ortalama günlük zikrini hesaplar
     * - Bugünkü zikir önceki günlerin ortalamasının %50'sinden azsa veya belirgin bir düşüş varsa tetikler
     */
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

        // Bugünkü samimi çekilen zikir toplamı
        val todayAmount = manualHistory
            .filter { it.dateKey == todayKey }
            .sumOf { it.amount }

        // Önceki 7 günün tarihlerini belirle
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

        // Kullanıcının daha önceki günlerde çekilmiş en az 2 günlük zikir geçmişi olmalı
        if (pastDaysWithActivity.size < 2) {
            return false
        }

        val pastAverage = pastDaysWithActivity.values.average()
        if (pastAverage < 50) {
            return false
        }

        // Eğer bugünkü çekilen miktar, geçmiş ortalamanın yarısından azsa veya belirgin azaldıysa
        val isSignificantlyReduced = todayAmount < (pastAverage * 0.5)

        return isSignificantlyReduced
    }

    companion object {
        const val EXTRA_TYPE = "extra_type"
        const val EXTRA_SLOT_ID = "extra_slot_id"
        const val TYPE_DAILY_REMINDER = "type_daily_reminder"
        const val TYPE_INACTIVITY = "type_inactivity"
        const val TYPE_TARGET_REMINDER = "type_target_reminder"
        const val TYPE_ADAPTIVE_CHECK = "type_adaptive_check"
        const val NOTIFICATION_ID_ADAPTIVE = 7777
    }
}
