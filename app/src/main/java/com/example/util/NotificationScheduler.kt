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

    fun scheduleDailyReminders(slots: List<ReminderSlot>, isEnabled: Boolean) {
        // Cancel existing slot alarms first
        cancelAllReminders(slots)

        if (!isEnabled) return

        slots.forEach { slot ->
            if (slot.isEnabled) {
                scheduleSlot(slot)
            }
        }
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

        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            putExtra(ReminderAlarmReceiver.EXTRA_TYPE, ReminderAlarmReceiver.TYPE_DAILY_REMINDER)
            putExtra(ReminderAlarmReceiver.EXTRA_SLOT_ID, slot.id.toInt())
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            slot.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        } catch (e: Exception) {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    fun scheduleInactivityAlert(isEnabled: Boolean) {
        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            putExtra(ReminderAlarmReceiver.EXTRA_TYPE, ReminderAlarmReceiver.TYPE_INACTIVITY)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            9999,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)

        if (isEnabled) {
            val triggerTime = System.currentTimeMillis() + (24 * 60 * 60 * 1000L)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }
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
            8888,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)

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

    private fun cancelAllReminders(slots: List<ReminderSlot>) {
        slots.forEach { slot ->
            val intent = Intent(context, ReminderAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                slot.id.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
    }
}
