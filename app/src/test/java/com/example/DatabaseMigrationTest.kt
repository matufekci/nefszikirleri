package com.example

import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import com.example.data.local.AppDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Room migration testi.
 *
 * Neden kritik: Uygulamayi halihazirda kuran kullanicilarin telefonunda v5/v6/v7
 * semasi duruyor. Migration'lardan biri hatali olursa kullanici guncellemeden
 * sonra ya crash alir ya da zikir gecmisi sessizce kaybolur. Bu senaryo JVM
 * testinde yakalanamiyordu; asagidaki testler gercek SQLite uzerinde eski semayi
 * kurup migration'lari calistiriyor ve sonucu v8 semasiyla birebir dogruluyor.
 *
 * Semalar `app/schemas` altinda ve build.gradle'da test asset'i olarak tanimli
 * (`sourceSets.test.assets.srcDirs`), MigrationTestHelper bunlari oradan okur.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DatabaseMigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    private val dbName = "migration-test-db"

    @Test
    fun migrate5To8_zikirVeGecmisVerisiKorunur() {
        helper.createDatabase(dbName, 5).apply {
            execSQL("INSERT INTO zikirs (id, target, count, startedAt, completedAt) VALUES (1, 100, 42, NULL, NULL)")
            execSQL(
                "INSERT INTO zikir_history (id, zikirId, amount, type, timestamp, dateKey) " +
                    "VALUES (10, 1, 42, 'add', 1700000000000, '2026-09-10')"
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(
            dbName,
            8,
            true,
            AppDatabase.MIGRATION_5_6,
            AppDatabase.MIGRATION_6_7,
            AppDatabase.MIGRATION_7_8
        )

        db.query("SELECT count FROM zikirs WHERE id = 1").use { c ->
            assertTrue("zikir satiri migration sonrasi durmali", c.moveToFirst())
            assertEquals(42, c.getInt(0))
        }
        db.query("SELECT amount, eventId FROM zikir_history WHERE id = 10").use { c ->
            assertTrue("gecmis satiri migration sonrasi durmali", c.moveToFirst())
            assertEquals(42L, c.getLong(0))
            assertTrue("eventId migration tarafindan doldurulmali", c.getString(1).isNotBlank())
        }
        db.close()
    }

    @Test
    fun migrate7To8_eventIdlerBenzersizVeBosOlmaz() {
        helper.createDatabase(dbName, 7).apply {
            execSQL("INSERT INTO zikirs (id, target, count, startedAt, completedAt) VALUES (1, 100, 0, NULL, NULL)")
            repeat(50) { i ->
                execSQL(
                    "INSERT INTO zikir_history (id, zikirId, amount, type, timestamp, dateKey) " +
                        "VALUES (${i + 1}, 1, 1, 'add', ${1700000000000L + i}, '2026-09-10')"
                )
            }
            close()
        }

        val db = helper.runMigrationsAndValidate(dbName, 8, true, AppDatabase.MIGRATION_7_8)

        db.query("SELECT eventId FROM zikir_history").use { c ->
            val ids = mutableListOf<String>()
            while (c.moveToNext()) ids.add(c.getString(0))
            assertEquals(50, ids.size)
            assertEquals("eventId'ler benzersiz olmali (unique index var)", 50, ids.toSet().size)
            assertTrue("hicbir eventId bos kalmamali", ids.none { it.isBlank() })
        }
        db.close()
    }

    @Test
    fun migrate6To8_pendingOperationsTablosuOlusur() {
        helper.createDatabase(dbName, 6).apply {
            execSQL("INSERT INTO zikirs (id, target, count, startedAt, completedAt) VALUES (3, 500, 7, NULL, NULL)")
            close()
        }

        val db = helper.runMigrationsAndValidate(
            dbName,
            8,
            true,
            AppDatabase.MIGRATION_6_7,
            AppDatabase.MIGRATION_7_8
        )

        // v6'da bu tablo yoktu; v7 ile gelmeli ve yazilabilir olmali.
        db.execSQL(
            "INSERT INTO pending_operations (operationId, zikirId, amount, timestamp, dateKey, status) " +
                "VALUES ('op-1', 3, 5, 1700000000000, '2026-09-10', 'pending')"
        )
        db.query("SELECT status FROM pending_operations WHERE operationId = 'op-1'").use { c ->
            assertTrue(c.moveToFirst())
            assertEquals("pending", c.getString(0))
        }
        db.query("SELECT count FROM zikirs WHERE id = 3").use { c ->
            assertTrue(c.moveToFirst())
            assertEquals(7, c.getInt(0))
        }
        db.close()
    }
}
