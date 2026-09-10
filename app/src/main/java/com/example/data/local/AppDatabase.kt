package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AppSettings
import com.example.data.model.ReminderSlot
import com.example.data.model.Zikir
import com.example.data.model.ZikirContent
import com.example.data.model.ZikirHistory
import com.example.data.model.PendingOperation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


import androidx.room.migration.Migration

@Database(
    entities = [Zikir::class, ZikirHistory::class, ReminderSlot::class, AppSettings::class, PendingOperation::class],
    version = 8,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun zikirDao(): ZikirDao
    abstract fun historyDao(): HistoryDao
    abstract fun reminderDao(): ReminderDao
    abstract fun settingsDao(): SettingsDao
    abstract fun pendingOperationDao(): PendingOperationDao


    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE app_settings ADD COLUMN acknowledgedBadges TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Version 2 and 3 have the same schema in production.
                // This migration exists purely to handle version bumps that occurred
                // without actual schema changes. No SQL commands are needed.
                android.util.Log.i("AppDatabase", "Migrating from v2 to v3: Schema unchanged.")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE app_settings ADD COLUMN autoReorderSettings INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE app_settings ADD COLUMN settingsUsageStats TEXT NOT NULL DEFAULT '{}'")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create the new table
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `zikir_history_new` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`zikirId` INTEGER NOT NULL, " +
                            "`amount` INTEGER NOT NULL, " +
                            "`type` TEXT NOT NULL, " +
                            "`timestamp` INTEGER NOT NULL, " +
                            "`dateKey` TEXT NOT NULL, " +
                            "FOREIGN KEY(`zikirId`) REFERENCES `zikirs`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                // Ensure all referenced zikirIds exist in zikirs so no history is lost and FK constraints are satisfied
                db.execSQL(
                    "INSERT OR IGNORE INTO `zikirs` (`id`, `target`, `count`, `startedAt`, `completedAt`) " +
                            "SELECT DISTINCT `zikirId`, 100, 0, NULL, NULL FROM `zikir_history` " +
                            "WHERE `zikirId` NOT IN (SELECT `id` FROM `zikirs`)"
                )
                // Copy the data
                db.execSQL(
                    "INSERT INTO `zikir_history_new` (`id`, `zikirId`, `amount`, `type`, `timestamp`, `dateKey`) " +
                            "SELECT `id`, `zikirId`, `amount`, `type`, `timestamp`, `dateKey` FROM `zikir_history`"
                )
                // Remove the old table
                db.execSQL("DROP TABLE `zikir_history`")
                // Change the table name to the correct one
                db.execSQL("ALTER TABLE `zikir_history_new` RENAME TO `zikir_history`")
                // Create indices
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_zikir_history_zikirId` ON `zikir_history` (`zikirId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_zikir_history_dateKey` ON `zikir_history` (`dateKey`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_zikir_history_timestamp` ON `zikir_history` (`timestamp`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_zikir_history_zikirId_dateKey` ON `zikir_history` (`zikirId`, `dateKey`)")
            }
        }

        val MIGRATION_1_4 = object : Migration(1, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE app_settings ADD COLUMN acknowledgedBadges TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE app_settings ADD COLUMN autoReorderSettings INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE app_settings ADD COLUMN settingsUsageStats TEXT NOT NULL DEFAULT '{}'")
            }
        }

        val MIGRATION_2_4 = object : Migration(2, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE app_settings ADD COLUMN autoReorderSettings INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE app_settings ADD COLUMN settingsUsageStats TEXT NOT NULL DEFAULT '{}'")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_zikir_history_timestamp` ON `zikir_history` (`timestamp`)")
            }
        }

        val MIGRATION_1_6 = object : Migration(1, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE app_settings ADD COLUMN acknowledgedBadges TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE app_settings ADD COLUMN autoReorderSettings INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE app_settings ADD COLUMN settingsUsageStats TEXT NOT NULL DEFAULT '{}'")
                
                // Rebuild zikir_history to include ON DELETE CASCADE foreign key
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `zikir_history_new` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`zikirId` INTEGER NOT NULL, " +
                            "`amount` INTEGER NOT NULL, " +
                            "`type` TEXT NOT NULL, " +
                            "`timestamp` INTEGER NOT NULL, " +
                            "`dateKey` TEXT NOT NULL, " +
                            "FOREIGN KEY(`zikirId`) REFERENCES `zikirs`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                // Ensure all referenced zikirIds exist in zikirs so no history is lost and FK constraints are satisfied
                db.execSQL(
                    "INSERT OR IGNORE INTO `zikirs` (`id`, `target`, `count`, `startedAt`, `completedAt`) " +
                            "SELECT DISTINCT `zikirId`, 100, 0, NULL, NULL FROM `zikir_history` " +
                            "WHERE `zikirId` NOT IN (SELECT `id` FROM `zikirs`)"
                )
                // Copy the data
                db.execSQL(
                    "INSERT INTO `zikir_history_new` (`id`, `zikirId`, `amount`, `type`, `timestamp`, `dateKey`) " +
                            "SELECT `id`, `zikirId`, `amount`, `type`, `timestamp`, `dateKey` FROM `zikir_history`"
                )
                // Remove the old table
                db.execSQL("DROP TABLE `zikir_history`")
                // Change the table name to the correct one
                db.execSQL("ALTER TABLE `zikir_history_new` RENAME TO `zikir_history`")
                
                // Create all required indices for v6
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_zikir_history_zikirId` ON `zikir_history` (`zikirId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_zikir_history_dateKey` ON `zikir_history` (`dateKey`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_zikir_history_timestamp` ON `zikir_history` (`timestamp`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_zikir_history_zikirId_dateKey` ON `zikir_history` (`zikirId`, `dateKey`)")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `pending_operations` (" +
                            "`operationId` TEXT NOT NULL, " +
                            "`zikirId` INTEGER NOT NULL, " +
                            "`amount` INTEGER NOT NULL, " +
                            "`timestamp` INTEGER NOT NULL, " +
                            "`dateKey` TEXT NOT NULL, " +
                            "`status` TEXT NOT NULL, " +
                            "PRIMARY KEY(`operationId`))"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_pending_operations_status` ON `pending_operations` (`status`)")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add eventId column with default empty string
                db.execSQL("ALTER TABLE `zikir_history` ADD COLUMN `eventId` TEXT NOT NULL DEFAULT ''")

                // Populate existing records with non-empty deterministic eventId values based on id/timestamp
                val cursor = db.query("SELECT `id`, `zikirId`, `timestamp` FROM `zikir_history`")
                val updates = mutableListOf<Pair<Long, String>>()
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(0)
                    val zikirId = cursor.getInt(1)
                    val timestamp = cursor.getLong(2)
                    val generatedEventId = "migrated_${id}_${zikirId}_${timestamp}_${java.util.UUID.randomUUID().toString().replace("-", "").take(8)}"
                    updates.add(Pair(id, generatedEventId))
                }
                cursor.close()

                for ((id, eventId) in updates) {
                    db.execSQL("UPDATE `zikir_history` SET `eventId` = ? WHERE `id` = ?", arrayOf<Any>(eventId, id))
                }

                // Create unique index on eventId
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_zikir_history_eventId` ON `zikir_history` (`eventId`)")
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nefs_zikir_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_1_4, MIGRATION_2_4, MIGRATION_1_6, MIGRATION_6_7, MIGRATION_7_8)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
