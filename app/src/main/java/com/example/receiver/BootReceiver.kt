package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.util.NotificationScheduler
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
                    // Hareketsizlik emniyet ağı ayarlardan bağımsız olarak her açılışta kurulur.
                    NotificationScheduler(context).scheduleInactivityAlert(true)

                    // 20:00 / 22:30 günlük hedef hatırlatma zincirini yeniden kur.
                    NotificationScheduler(context).scheduleDailyTargetReminders()

                    // Tempo matematiğini yürüten günlük değerlendirmeyi yeniden planla.
                    com.example.NefsApplication.scheduleDailyEvaluation(context)
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
