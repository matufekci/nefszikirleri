package com.example.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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

    companion object {
        const val REQUEST_CODE_INACTIVITY = 9999
    }
}
