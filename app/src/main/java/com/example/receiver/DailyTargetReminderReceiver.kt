package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.model.AppStrings
import com.example.util.AdaptiveReminderManager
import com.example.util.EveningReminderMessages
import com.example.util.NumberFormatter
import com.example.util.NotificationScheduler
import java.util.Calendar
import kotlinx.coroutines.launch

/**
 * Günlük hedef hatırlatıcısı (ayar gerektirmez, her kurulumda aktiftir):
 *
 * - 20:00 tipi: her akşam hedef hatırlatması. Günlük hedefin %25'inden azı
 *   kalmışsa "son viraj" motivasyon metni (kalan sayısı ile), aksi halde
 *   genel akşam hatırlatması — havuzdan her seferinde farklı bir metin.
 * - 22:30 tipi: yalnızca hedefin %25'inden azı kalmış VE hâlâ >0 ise geceye
 *   yakın son motivasyon dokunuşu.
 *
 * Her tetikleme bir sonraki günün alarmlarını yeniden kurar; uygulama
 * açılışında ve boot'ta da kurulur (çift kurulum FLAG_UPDATE_CURRENT ile
 * zararsızdır).
 */
class DailyTargetReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra(EXTRA_TYPE) ?: TYPE_EVENING
        val pendingResult = goAsync()

        com.example.NefsApplication.applicationScope.launch {
            try {
                // Yarınki 20:00'i ve (bugün henüz geçilmediyse) 22:30'u kur
                NotificationScheduler(context).scheduleDailyTargetReminders()

                val db = AppDatabase.getDatabase(context)
                val settings = db.settingsDao().getSettingsDirect()
                val lang = settings?.lang ?: AdaptiveReminderManager.getAppLanguage(context)
                val strings = AppStrings.get(lang)

                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                        ?: return@launch

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val granted = androidx.core.content.ContextCompat.checkSelfPermission(
                        context, android.Manifest.permission.POST_NOTIFICATIONS
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                    if (!granted) return@launch
                }

                val target = (settings?.dailyTarget ?: 0L).coerceAtLeast(1L)
                val cal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val todayTotal = db.historyDao()
                    .getDailyStatsDirect(cal.timeInMillis)
                    .filter { it.dateKey == NumberFormatter.getDateKey() }
                    .sumOf { it.total }

                val remaining = target - todayTotal
                if (remaining <= 0L) return@launch // hedef tamamlandı — rahatsız etme

                val threshold = target / 4L // %25
                val isFinishLine = remaining <= threshold

                // 22:30 dokunuşu yalnızca "son viraj" durumunda gönderilir
                if (type == TYPE_NUDGE && !isFinishLine) return@launch

                val (title, content) = if (isFinishLine) {
                    com.example.ui.UiText.finishNotifTitle.get(lang) to
                        EveningReminderMessages.pick(
                            context, lang, EveningReminderMessages.Type.FINISH
                        ).format(remaining)
                } else {
                    com.example.ui.UiText.eveningNotifTitle.get(lang) to
                        EveningReminderMessages.pick(
                            context, lang, EveningReminderMessages.Type.EVENING
                        )
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

                val launchIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                val notificationId =
                    if (type == TYPE_EVENING) NOTIFICATION_ID_EVENING else NOTIFICATION_ID_NUDGE
                val pendingIntent = PendingIntent.getActivity(
                    context, notificationId, launchIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val notification = androidx.core.app.NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setStyle(androidx.core.app.NotificationCompat.BigTextStyle().bigText(content))
                    .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build()

                notificationManager.notify(notificationId, notification)
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                if (com.example.BuildConfig.DEBUG) {
                    android.util.Log.e("DailyTargetReminder", "dispatch failed", e)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val EXTRA_TYPE = "reminder_type"
        const val TYPE_EVENING = "evening_20"
        const val TYPE_NUDGE = "nudge_2230"
        const val NOTIFICATION_ID_EVENING = 8801
        const val NOTIFICATION_ID_NUDGE = 8802
    }
}
