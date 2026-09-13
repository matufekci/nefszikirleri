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
import kotlinx.coroutines.launch

/**
 * Yalnızca HAREKETSİZLİK alarmını işler.
 *
 * Günlük/hedefli/slotlu hatırlatıcı ayarları kaldırıldı; bildirim temposunu
 * artık DailyEvaluationWorker içindeki tempo matematiği (1.140.000 zikir / 6 ay)
 * çekilen zikirlere göre otomatik belirler. Bu alıcı, uygulama hiç açılmadığında
 * devreye giren emniyet ağıdır: son zikirden [AdaptiveReminderManager.INACTIVITY_TRIGGER_DAYS]
 * gün sonra çalar, son [AdaptiveReminderManager.INACTIVITY_THRESHOLD_DAYS] günde
 * manuel kayıt yoksa sırayla uyarı/müjde ayeti gönderir.
 */
class ReminderAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra(EXTRA_TYPE) ?: TYPE_INACTIVITY
        if (type != TYPE_INACTIVITY) {
            // Eski sürümlerden kalan alarm tipleri (slot/hedef/adaptif) artık işlenmez.
            return
        }
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

                // Gerçek hareketsizlik kontrolü: eşik gün sayısı içinde manuel zikir
                // kaydı varsa kullanıcı aktiftir, bildirim gönderilmez.
                val inactivitySince = System.currentTimeMillis() -
                    (AdaptiveReminderManager.INACTIVITY_THRESHOLD_DAYS * 24L * 60 * 60 * 1000L)
                val recentManual = db.historyDao().getRecentManualHistoryDirect(inactivitySince)
                if (recentManual.isNotEmpty()) {
                    com.example.util.NotificationScheduler(context).scheduleInactivityAlert(true)
                    return@launch
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

                // 5 uyarı + 5 müjde ayeti sırayla dönüşümlü gösterilir.
                val verse = AdaptiveReminderManager.getInactivityVerse(context)
                val title = strings.inactivityNotifTitle
                val content = "${verse.surah}\n\"${verse.verseText}\""
                val notificationId = NOTIFICATION_ID_INACTIVITY

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

                notificationManager.notify(notificationId, notification)

                // Bir sonraki hareketsizlik döngüsünü kur
                com.example.util.NotificationScheduler(context).scheduleInactivityAlert(true)
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                if (com.example.BuildConfig.DEBUG) {
                    android.util.Log.e("ReminderAlarmReceiver", "Failed to dispatch inactivity notification", e)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val EXTRA_TYPE = "extra_type"
        const val TYPE_INACTIVITY = "type_inactivity"
        const val NOTIFICATION_ID_INACTIVITY = 9999
    }
}
