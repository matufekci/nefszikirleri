package com.example.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.receiver.DailyTargetReminderReceiver
import com.example.receiver.ReminderAlarmReceiver

/**
 * Yalnızca hareketsizlik alarmını yönetir.
 *
 * Slotlu günlük hatırlatıcılar ve hedef hatırlatıcısı kaldırıldı; bildirim
 * temposunu DailyEvaluationWorker içindeki tempo matematiği belirler.
 * Bu alarm, uygulama hiç açılmadığında devreye giren emniyet ağıdır.
 */
class NotificationScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleInactivityAlert(isEnabled: Boolean) {
        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            putExtra(ReminderAlarmReceiver.EXTRA_TYPE, ReminderAlarmReceiver.TYPE_INACTIVITY)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_INACTIVITY,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()

        if (isEnabled) {
            // Son zikirden INACTIVITY_TRIGGER_DAYS gün sonrasına kurulur; her zikirde
            // yeniden kurulduğu için alarm daima son aktiviteyi takip eder.
            val triggerTime = System.currentTimeMillis() +
                (AdaptiveReminderManager.INACTIVITY_TRIGGER_DAYS * 24L * 60 * 60 * 1000L)
            try {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } catch (e: Exception) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        }
    }

    /** Tüm bildirim alarmlarını iptal eder (yedek geri yükleme/sıfırlama sonrası temizlik). */
    fun cancelAllScheduledAlarms() {
        scheduleInactivityAlert(false)
    }

    /**
     * Günlük hedef hatırlatmalarını kurar: 20:00 (akşam) + 22:30 (gece dokunuşu).
     * Saati geçtiyse otomatik olarak yarına kurulur; her tetikleme ve uygulama
     * açılışı/boot yeniden çağırdığı için zincir hiç kopmaz.
     */
    fun scheduleDailyTargetReminders() {
        scheduleAtHour(20, 0, DailyTargetReminderReceiver.TYPE_EVENING, REQUEST_CODE_EVENING)
        scheduleAtHour(22, 30, DailyTargetReminderReceiver.TYPE_NUDGE, REQUEST_CODE_NUDGE)
    }

    private fun scheduleAtHour(
        hour: Int,
        minute: Int,
        type: String,
        requestCode: Int
    ) {
        val intent = Intent(context, DailyTargetReminderReceiver::class.java).apply {
            putExtra(DailyTargetReminderReceiver.EXTRA_TYPE, type)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)

        val now = java.util.Calendar.getInstance()
        val trigger = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
            if (!after(now)) add(java.util.Calendar.DAY_OF_YEAR, 1)
        }
        try {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, trigger.timeInMillis, pendingIntent
            )
        } catch (e: Exception) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, trigger.timeInMillis, pendingIntent)
        }
    }

    companion object {
        const val REQUEST_CODE_INACTIVITY = 9999
        const val REQUEST_CODE_EVENING = 9997
        const val REQUEST_CODE_NUDGE = 9996
    }
}
