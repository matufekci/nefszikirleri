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
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    val settings = db.settingsDao().getSettingsDirect()
                    val slots = db.reminderDao().getAllSlotsList()
                    val scheduler = NotificationScheduler(context)

                    if (settings?.reminderEnabled == true) {
                        scheduler.scheduleDailyReminders(slots, true)
                    }

                    if (settings?.inactivityAlertEnabled == true) {
                        scheduler.scheduleInactivityAlert(true)
                    }
                    // Akıllı zikir azalma takip servisini yeniden başlat
                    com.example.util.AdaptiveReminderManager.schedulePeriodicEvaluation(context)
                } catch (e: Exception) {
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
