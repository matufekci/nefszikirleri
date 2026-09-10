package com.example.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.model.ReminderSlot
import com.example.receiver.ReminderAlarmReceiver
import java.util.Calendar

class NotificationScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Günlük zikir hatırlatıcılarını planlar ve eski/silinmiş (orphan) slot alarmlarını temizler.
     * Database'deki current enabled slot seti ile scheduler'daki active alarm seti karşılaştırılır.
     */
    fun scheduleDailyReminders(slots: List<ReminderSlot>, isEnabled: Boolean) {
        val currentEnabledSlots = if (isEnabled) {
            slots.filter { it.isEnabled }
        } else {
            emptyList()
        }
        val currentActiveSlotIds = currentEnabledSlots.map { it.id }.toSet()
        val previouslyScheduledSlotIds = getActiveScheduledSlotIds()

        // Hem önceden kaydedilmiş hem de mevcut listede bulunan tüm slotları iptal et (orphan ve değişen alarmlar dahil)
        val allSlotIdsToClear = previouslyScheduledSlotIds + slots.map { it.id }
        allSlotIdsToClear.forEach { slotId ->
            cancelSlotAlarm(slotId)
        }

        if (isEnabled) {
            currentEnabledSlots.forEach { slot ->
                scheduleSlot(slot)
            }
        }

        // Scheduler'ın aktif alarm setini güncelle
        saveActiveScheduledSlotIds(currentActiveSlotIds)
    }

    @Synchronized
    fun getActiveScheduledSlotIds(): Set<Long> {
        return prefs.getStringSet(KEY_SCHEDULED_SLOT_IDS, emptySet())
            ?.mapNotNull { it.toLongOrNull() }
            ?.toSet() ?: emptySet()
    }

    @Synchronized
    private fun saveActiveScheduledSlotIds(slotIds: Set<Long>) {
        // Use commit for critical alarm tracking to ensure persistence before process death
        prefs.edit()
            .putStringSet(KEY_SCHEDULED_SLOT_IDS, slotIds.map { it.toString() }.toSet())
            .apply()
    }

    fun cancelAllScheduledAlarms() {
        val scheduled = getActiveScheduledSlotIds()
        scheduled.forEach { cancelSlotAlarm(it) }
        saveActiveScheduledSlotIds(emptySet())
        // Also cancel inactivity and target
        scheduleInactivityAlert(false)
        scheduleTargetReminder(false)
    }

    private fun scheduleSlot(slot: ReminderSlot) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, slot.hour)
            set(Calendar.MINUTE, slot.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val requestCode = getSlotRequestCode(slot.id)

        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            putExtra(ReminderAlarmReceiver.EXTRA_TYPE, ReminderAlarmReceiver.TYPE_DAILY_REMINDER)
            putExtra(ReminderAlarmReceiver.EXTRA_SLOT_ID, requestCode)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            // Check if we can schedule exact alarms (Android 12+), otherwise use inexact
            val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager.canScheduleExactAlarms()
            } else true

            if (canExact) {
                // Use exact for first trigger, then repeating inexact for battery
                try {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                } catch (e: Exception) {
                    alarmManager.setInexactRepeating(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        AlarmManager.INTERVAL_DAY,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            try {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } catch (e2: Exception) {
                if (com.example.BuildConfig.DEBUG) {
                    android.util.Log.e("NotificationScheduler", "Failed to schedule slot $slot", e2)
                }
            }
        }
    }

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
            val triggerTime = System.currentTimeMillis() + (24 * 60 * 60 * 1000L)
            try {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } catch (e: Exception) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        }
    }

    fun scheduleTargetReminder(isEnabled: Boolean) {
        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            putExtra(ReminderAlarmReceiver.EXTRA_TYPE, ReminderAlarmReceiver.TYPE_TARGET_REMINDER)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_TARGET,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()

        if (isEnabled) {
            // Schedule for 21:00 (9 PM) every day
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 21)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (before(Calendar.getInstance())) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            try {
                alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                )
            } catch (e: Exception) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            }
        }
    }

    private fun cancelSlotAlarm(slotId: Long) {
        val requestCode = getSlotRequestCode(slotId)
        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            putExtra(ReminderAlarmReceiver.EXTRA_TYPE, ReminderAlarmReceiver.TYPE_DAILY_REMINDER)
            putExtra(ReminderAlarmReceiver.EXTRA_SLOT_ID, requestCode)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    private fun cancelAllReminders(slots: List<ReminderSlot>) {
        slots.forEach { slot ->
            cancelSlotAlarm(slot.id)
        }
    }

    companion object {
        const val PREFS_NAME = "notification_scheduler_prefs"
        const val KEY_SCHEDULED_SLOT_IDS = "scheduled_slot_ids"
        const val REQUEST_CODE_INACTIVITY = 9999
        const val REQUEST_CODE_TARGET = 8888
        const val REQUEST_CODE_BASE_SLOT = 10000
        private const val MAX_SLOT_OFFSET = Int.MAX_VALUE - REQUEST_CODE_BASE_SLOT

        /**
         * Slot ID'lerinden deterministik, çakışmasız ve taşma (overflow) korumalı PendingIntent request code üretir.
         * Modulo tabanlı dar aralık yerine doğrudan 1-e-1 doğrusal eşleme kullanır; böylece farklı slot ID'leri
         * birbirleriyle veya sistem kodlarıyla (8888, 9999) çakışmaz.
         */
        fun getSlotRequestCode(slotId: Long): Int {
            val positiveId = if (slotId == Long.MIN_VALUE) {
                Long.MAX_VALUE
            } else if (slotId < 0L) {
                -slotId
            } else {
                slotId
            }

            val offset = if (positiveId <= MAX_SLOT_OFFSET) {
                positiveId.toInt()
            } else {
                // Integer overflow koruması: 64-bit ID'yi güvenli 31-bit pozitif offset alanına indirger
                val folded = ((positiveId xor (positiveId ushr 32)) and 0x7FFFFFFF)
                (folded % MAX_SLOT_OFFSET).toInt()
            }

            return REQUEST_CODE_BASE_SLOT + offset
        }
    }
}
