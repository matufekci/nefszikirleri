package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.AppDatabase
import com.example.util.NotificationScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == Intent.ACTION_TIMEZONE_CHANGED ||
            action == Intent.ACTION_TIME_CHANGED
        ) {
            val pendingResult = goAsync()
            com.example.NefsApplication.applicationScope.launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    val settings = db.settingsDao().getSettingsDirect()
                    val slots = db.reminderDao().getAllSlotsList()
                    val scheduler = NotificationScheduler(context)

                    if (settings?.reminderEnabled == true) {
                        scheduler.scheduleDailyReminders(slots, true)
                    } else {
                        // Ensure orphan alarms are cleaned even if disabled
                        scheduler.scheduleDailyReminders(emptyList(), false)
                    }

                    // Manevi hatırlatıcı ayarlardan bağımsız olarak her açılışta kurulur.
                    scheduler.scheduleInactivityAlert(true)

                    if (settings?.targetReminderEnabled == true) {
                        scheduler.scheduleTargetReminder(true)
                    }

                    // Reschedule WorkManager periodic evaluation
                    com.example.NefsApplication.scheduleDailyEvaluation(context)

                    // Legacy adaptive check - now handled by WorkManager, keep for backward compat
                    com.example.util.AdaptiveReminderManager.schedulePeriodicEvaluation(context)
                } catch (e: Exception) {
                    if (e is kotlinx.coroutines.CancellationException) throw e
                    if (com.example.BuildConfig.DEBUG) {
                        android.util.Log.e("BootReceiver", "Failed to reschedule alarms on boot", e)
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
