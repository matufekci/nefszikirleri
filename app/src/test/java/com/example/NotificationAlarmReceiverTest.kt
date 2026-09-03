package com.example

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.room.Room
import com.example.data.local.AppDatabase
import com.example.data.model.AppSettings
import com.example.data.model.ReminderSlot
import com.example.receiver.BootReceiver
import com.example.receiver.ReminderAlarmReceiver
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
        val slots = listOf(
            ReminderSlot(id = 1, hour = 8, minute = 0, isEnabled = true),
            ReminderSlot(id = 2, hour = 18, minute = 30, isEnabled = false)
        )

        // Should execute smoothly without throwing exceptions or security errors
        scheduler.scheduleDailyReminders(slots, isEnabled = true)
        scheduler.scheduleInactivityAlert(isEnabled = true)
        scheduler.scheduleTargetReminder(isEnabled = true)

        scheduler.scheduleDailyReminders(slots, isEnabled = false)
        scheduler.scheduleInactivityAlert(isEnabled = false)
        scheduler.scheduleTargetReminder(isEnabled = false)
    }

    @Test
    fun testDatabase_SettingsAndSlotsPersistence() = runBlocking {
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
