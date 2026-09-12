package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.room.Room
import com.example.data.local.AppDatabase
import com.example.data.model.AppSettings
import com.example.data.model.ReminderSlot
import com.example.util.NotificationScheduler
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NotificationAlarmReceiverTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testNotificationScheduler_ScheduleAndCancel() {
        val scheduler = NotificationScheduler(context)

        // Yalnızca hareketsizlik emniyet ağı planlanır; slot/hedef alarmları kaldırıldı.
        scheduler.scheduleInactivityAlert(isEnabled = true)
        scheduler.scheduleInactivityAlert(isEnabled = false)
        scheduler.cancelAllScheduledAlarms()
    }

    @Test
    fun testDatabase_SettingsAndSlotsPersistence() = runBlocking {
        // Ayar kolonları ve slot tablosu yedek/şema uyumluluğu için korunuyor.
        database.settingsDao().insertOrUpdate(
            AppSettings(id = 1, reminderEnabled = true, inactivityAlertEnabled = true, targetReminderEnabled = true)
        )
        database.reminderDao().insert(ReminderSlot(id = 1, hour = 9, minute = 0, isEnabled = true))

        val settings = database.settingsDao().getSettingsDirect()
        val slots = database.reminderDao().getAllSlotsList()

        assertNotNull(settings)
        assertEquals(true, settings?.reminderEnabled)
        assertEquals(1, slots.size)
        assertEquals(9, slots[0].hour)
    }
}
