package com.example

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import java.io.File
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Room migration testi.
 *
 * Neden kritik: Uygulamayi halihazirda kuran kullanicilarin telefonunda v5/v6/v7
 * semasi duruyor. Migration'lardan biri hatali olursa kullanici guncellemeden
 * sonra ya crash alir ya da zikir gecmisi sessizce kaybolur.
 *
 * Neden MigrationTestHelper degil: MigrationTestHelper semalari ANDROID ASSET
 * olarak okur. `sourceSets.test.assets` uzerinden verilen klasor AGP tarafindan
 * Robolectric'in birlestirilmis asset'lerine tasınmıyor ve test
 * "Cannot find the schema file in the assets folder" ile patlıyor (CI'da
 * birebir goruldu). Bu yuzden semalar dosya sisteminden okunuyor — JVM
 * testlerinde calisma dizini modul kokudur — ve eski sema, Room'un kendi
 * urettigi `createSql` ifadeleriyle birebir kuruluyor. Boylece test,
 * gercekte kullanici telefonundaki semanin aynisini kurup migration'i
 * calistiriyor ve sonucu v8 semasiyla karsilastiriyor.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DatabaseMigrationTest {

    private lateinit var context: Context
    private lateinit var schemaDir: File

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        schemaDir = listOf(
            File("schemas/com.example.data.local.AppDatabase"),
            File("app/schemas/com.example.data.local.AppDatabase")
        ).firstOrNull { it.isDirectory }
            ?: throw IllegalStateException(
                "Room sema klasoru bulunamadi. Beklenen: schemas/com.example.data.local.AppDatabase"
            )
    }

    @After
    fun tearDown() {
        runCatching { context.databaseList().forEach { context.deleteDatabase(it) } }
    }

    // ---------------------------------------------------------------- altyapi

    private fun schema(version: Int): JSONObject =
        JSONObject(File(schemaDir, "$version.json").readText())

    /** Eski surumun semasini JSON'dan okuyup birebir kurar, acik DB dondurur. */
    private fun createDbAt(version: Int, name: String): SupportSQLiteDatabase {
        val json = schema(version)
        val config = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(name)
            .callback(object : SupportSQLiteOpenHelper.Callback(version) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    val entities = json.getJSONObject("database").getJSONArray("entities")
                    for (i in 0 until entities.length()) {
                        val entity = entities.getJSONObject(i)
                        val table = entity.getString("tableName")
                        db.execSQL(entity.getString("createSql").replace("\${TABLE_NAME}", table))
                        val indices = entity.optJSONArray("indices") ?: continue
                        for (k in 0 until indices.length()) {
                            db.execSQL(
                                indices.getJSONObject(k).getString("createSql")
                                    .replace("\${TABLE_NAME}", table)
                            )
                        }
                    }
                }

                override fun onUpgrade(db: SupportSQLiteDatabase, oldV: Int, newV: Int) = Unit
            })
            .build()
        return FrameworkSQLiteOpenHelperFactory().create(config).writableDatabase
    }

    private fun columns(db: SupportSQLiteDatabase, table: String): List<String> {
        val out = mutableListOf<String>()
        db.query("PRAGMA table_info(`$table`)").use { c ->
            while (c.moveToNext()) out.add(c.getString(1))
        }
        return out
    }

    /** index adi -> unique mi. PRAGMA index_list kolonlari: seq, name, unique, ... */
    private fun indices(db: SupportSQLiteDatabase, table: String): Map<String, Boolean> {
        val out = mutableMapOf<String, Boolean>()
        db.query("PRAGMA index_list(`$table`)").use { c ->
            while (c.moveToNext()) out[c.getString(1)] = c.getInt(2) == 1
        }
        return out
    }

    /** Canli DB'nin yapisi beklenen sema JSON'uyla birebir ortusuyor mu? */
    private fun assertMatchesSchema(db: SupportSQLiteDatabase, expected: JSONObject) {
        val entities = expected.getJSONObject("database").getJSONArray("entities")
        for (i in 0 until entities.length()) {
            val entity = entities.getJSONObject(i)
            val table = entity.getString("tableName")

            val fields = entity.getJSONArray("fields")
            val expectedCols = (0 until fields.length())
                .map { fields.getJSONObject(it).getString("columnName") }
            val actualCols = columns(db, table)
            // Siraya duyarsiz karsilastirma: Room'un kendi semasi da kolonlari
            // kume olarak dogrular. Bu sart, cunku ALTER TABLE ADD COLUMN kolonu
            // fiziksel olarak EN SONA ekler; ornek: 7->8 migration'i eventId'yi
            // ekler, oysa 8.json onu 2. sirada listeler.
            assertEquals("$table tablosunun kolon SAYISI semayla ortusmeli", expectedCols.size, actualCols.size)
            assertEquals(
                "$table tablosunun kolonlari semayla ortusmeli",
                expectedCols.sorted(),
                actualCols.sorted()
            )

            val actualIdx = indices(db, table)
            val expectedIdx = entity.optJSONArray("indices")
            if (expectedIdx != null) {
                for (k in 0 until expectedIdx.length()) {
                    val idx = expectedIdx.getJSONObject(k)
                    val idxName = idx.getString("name")
                    assertTrue("$table.$idxName indexi olusmali", actualIdx.containsKey(idxName))
                    assertEquals(
                        "$table.$idxName unique bayragi semayla ortusmeli",
                        idx.getBoolean("unique"),
                        actualIdx.getValue(idxName)
                    )
                }
            }
        }
    }

    // ------------------------------------------------------------------ testler

    @Test
    fun migrate5To8_zikirVeGecmisVerisiKorunur() {
        val db = createDbAt(5, "mig-5-8.db")
        db.execSQL("INSERT INTO zikirs (id, target, count, startedAt, completedAt) VALUES (1, 100, 42, NULL, NULL)")
        db.execSQL(
            "INSERT INTO zikir_history (id, zikirId, amount, type, timestamp, dateKey) " +
                "VALUES (10, 1, 42, 'add', 1700000000000, '2026-09-10')"
        )

        AppDatabase.MIGRATION_5_6.migrate(db)
        AppDatabase.MIGRATION_6_7.migrate(db)
        AppDatabase.MIGRATION_7_8.migrate(db)

        assertMatchesSchema(db, schema(8))

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
        val db = createDbAt(7, "mig-7-8.db")
        db.execSQL("INSERT INTO zikirs (id, target, count, startedAt, completedAt) VALUES (1, 100, 0, NULL, NULL)")
        repeat(50) { i ->
            db.execSQL(
                "INSERT INTO zikir_history (id, zikirId, amount, type, timestamp, dateKey) " +
                    "VALUES (${i + 1}, 1, 1, 'add', ${1700000000000L + i}, '2026-09-10')"
            )
        }

        AppDatabase.MIGRATION_7_8.migrate(db)

        assertMatchesSchema(db, schema(8))

        db.query("SELECT eventId FROM zikir_history").use { c ->
            val ids = mutableListOf<String>()
            while (c.moveToNext()) ids.add(c.getString(0))
            assertEquals(50, ids.size)
            assertEquals("eventId'ler benzersiz olmali (unique index var)", 50, ids.toSet().size)
            assertTrue("hicbir eventId bos kalmamali", ids.none { it.isBlank() })
        }
        db.close()
    }

    /** PRAGMA foreign_key_list -> (from kolonu, hedef tablo, on_delete) */
    private fun foreignKeys(db: SupportSQLiteDatabase, table: String): List<Triple<String, String, String>> {
        val out = mutableListOf<Triple<String, String, String>>()
        db.query("PRAGMA foreign_key_list(`$table`)").use { c ->
            // kolonlar: id, seq, table, from, to, on_update, on_delete, match
            while (c.moveToNext()) out.add(Triple(c.getString(3), c.getString(2), c.getString(6)))
        }
        return out
    }

    private fun count(db: SupportSQLiteDatabase, sql: String): Long =
        db.query(sql).use { c -> c.moveToFirst(); c.getLong(0) }

    /**
     * v1 semasi. 5.json'dan onceki surumlerin JSON'u repoda yok; v1 tablolari
     * ProductionRegressionSuiteTest.test25 ile ayni (uretimdeki ilk surum).
     */
    private fun createV1Db(name: String): SupportSQLiteDatabase {
        val config = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(name)
            .callback(object : SupportSQLiteOpenHelper.Callback(1) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL("CREATE TABLE IF NOT EXISTS `zikirs` (`id` INTEGER NOT NULL, `target` INTEGER NOT NULL, `count` INTEGER NOT NULL, `startedAt` INTEGER, `completedAt` INTEGER, PRIMARY KEY(`id`))")
                    db.execSQL("CREATE TABLE IF NOT EXISTS `zikir_history` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `zikirId` INTEGER NOT NULL, `amount` INTEGER NOT NULL, `type` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `dateKey` TEXT NOT NULL)")
                    db.execSQL("CREATE TABLE IF NOT EXISTS `reminder_slots` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `hour` INTEGER NOT NULL, `minute` INTEGER NOT NULL, `isEnabled` INTEGER NOT NULL)")
                    db.execSQL("CREATE TABLE IF NOT EXISTS `app_settings` (`id` INTEGER NOT NULL, `lang` TEXT NOT NULL, `themeName` TEXT NOT NULL, `countdownMode` INTEGER NOT NULL, `dailyTarget` INTEGER NOT NULL, `hapticEnabled` INTEGER NOT NULL, `fullScreenTap` INTEGER NOT NULL, `keepAwakeEnabled` INTEGER NOT NULL, `completedRounds` INTEGER NOT NULL, `reminderEnabled` INTEGER NOT NULL, `inactivityAlertEnabled` INTEGER NOT NULL, `selectedZikirId` INTEGER NOT NULL, `lastActiveTimestamp` INTEGER NOT NULL, `counterTexture` TEXT NOT NULL, `fontScale` REAL NOT NULL, `hapticTapMode` TEXT NOT NULL, `hapticMilestoneMode` TEXT NOT NULL, `targetReminderEnabled` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                }

                override fun onUpgrade(db: SupportSQLiteDatabase, oldV: Int, newV: Int) = Unit
            })
            .build()
        return FrameworkSQLiteOpenHelperFactory().create(config).writableDatabase
    }

    @Test
    fun migrate1To8_tamZincir_veriKorunur_eventIdBenzersiz_fkVeIndexlerOlusur() {
        val db = createV1Db("mig-1-8-chain.db")
        db.execSQL("INSERT INTO zikirs (id, target, count, startedAt, completedAt) VALUES (1, 70000, 33, 1000, NULL)")
        db.execSQL("INSERT INTO zikirs (id, target, count, startedAt, completedAt) VALUES (2, 80000, 80000, 1000, 9000)")
        // v1'de FK yok: yetim (orphan) gecmis satiri olabilir -> 4->5 migration'i
        // kaybetmemeli, eksik zikir satirini INSERT OR IGNORE ile uretmeli.
        db.execSQL("INSERT INTO zikir_history (id, zikirId, amount, type, timestamp, dateKey) VALUES (1, 1, 33, 'add', 1700000000000, '2026-09-01')")
        db.execSQL("INSERT INTO zikir_history (id, zikirId, amount, type, timestamp, dateKey) VALUES (2, 2, 80000, 'add', 1700000000000, '2026-09-01')")
        db.execSQL("INSERT INTO zikir_history (id, zikirId, amount, type, timestamp, dateKey) VALUES (3, 9, 5, 'add', 1700000000000, '2026-09-01')")
        // Ayni timestamp'li satirlar: eventId yine benzersiz olmali (PK id ile)
        db.execSQL("INSERT INTO zikir_history (id, zikirId, amount, type, timestamp, dateKey) VALUES (4, 1, 1, 'add', 1700000000000, '2026-09-01')")

        AppDatabase.MIGRATION_1_2.migrate(db)
        AppDatabase.MIGRATION_2_3.migrate(db)
        AppDatabase.MIGRATION_3_4.migrate(db)
        AppDatabase.MIGRATION_4_5.migrate(db)
        AppDatabase.MIGRATION_5_6.migrate(db)
        AppDatabase.MIGRATION_6_7.migrate(db)
        AppDatabase.MIGRATION_7_8.migrate(db)

        assertMatchesSchema(db, schema(8))

        // Veri kaybi yok: 4 gecmis satiri + yetim icin uretilen zikir 9
        assertEquals(4L, count(db, "SELECT COUNT(*) FROM zikir_history"))
        assertEquals(1L, count(db, "SELECT COUNT(*) FROM zikirs WHERE id = 9"))
        assertEquals(33L, count(db, "SELECT count FROM zikirs WHERE id = 1"))
        assertEquals(9000L, count(db, "SELECT completedAt FROM zikirs WHERE id = 2"))

        // eventId: bos yok, benzersiz
        assertEquals(0L, count(db, "SELECT COUNT(*) FROM zikir_history WHERE eventId IS NULL OR eventId = ''"))
        assertEquals(4L, count(db, "SELECT COUNT(DISTINCT eventId) FROM zikir_history"))

        // FK: zikir_history.zikirId -> zikirs.id, ON DELETE CASCADE
        val fks = foreignKeys(db, "zikir_history")
        assertEquals("zikir_history tam olarak 1 FK tasimali", 1, fks.size)
        assertEquals(Triple("zikirId", "zikirs", "CASCADE"), fks.first())

        // Indexler (unique bayraklari dahil)
        val idx = indices(db, "zikir_history")
        assertEquals(false, idx["index_zikir_history_zikirId"])
        assertEquals(false, idx["index_zikir_history_dateKey"])
        assertEquals(false, idx["index_zikir_history_timestamp"])
        assertEquals(false, idx["index_zikir_history_zikirId_dateKey"])
        assertEquals(true, idx["index_zikir_history_eventId"])
        assertEquals(false, indices(db, "pending_operations")["index_pending_operations_status"])

        // Unique index gercekten uygulaniyor mu: ayni eventId ikinci kez girilemez
        val existing = db.query("SELECT eventId FROM zikir_history WHERE id = 1").use { c -> c.moveToFirst(); c.getString(0) }
        try {
            db.execSQL(
                "INSERT INTO zikir_history (eventId, zikirId, amount, type, timestamp, dateKey) VALUES (?, 1, 1, 'add', 1, '2026-09-02')",
                arrayOf<Any>(existing)
            )
            org.junit.Assert.fail("eventId unique index tekrar eden degeri reddetmeliydi")
        } catch (_: android.database.sqlite.SQLiteConstraintException) {
            // beklenen
        }

        // FK CASCADE calisiyor mu (Room acilista PRAGMA foreign_keys=ON yapar; burada elle aciyoruz)
        db.execSQL("PRAGMA foreign_keys = ON")
        db.execSQL("DELETE FROM zikirs WHERE id = 9")
        assertEquals("yetim zikir silinince gecmisi CASCADE ile gitmeli", 0L, count(db, "SELECT COUNT(*) FROM zikir_history WHERE zikirId = 9"))
        assertEquals("diger zikirlerin gecmisi durmali", 3L, count(db, "SELECT COUNT(*) FROM zikir_history"))

        // FK ihlali reddedilmeli: olmayan zikir icin gecmis yazilamaz
        try {
            db.execSQL("INSERT INTO zikir_history (eventId, zikirId, amount, type, timestamp, dateKey) VALUES ('x-1', 42, 1, 'add', 1, '2026-09-02')")
            org.junit.Assert.fail("FK olmayan zikirId'yi reddetmeliydi")
        } catch (_: android.database.sqlite.SQLiteConstraintException) {
            // beklenen
        }
        db.close()
    }

    @Test
    fun migrate1To6Dogrudan_sonra7ve8_zincirleAyniSemayiUretir() {
        // Dogrudan yollar (1->4, 2->4, 1->6) hala kayitli; bozulmadiklarini dogrula.
        val db = createV1Db("mig-1-6-direct.db")
        db.execSQL("INSERT INTO zikirs (id, target, count, startedAt, completedAt) VALUES (3, 500, 7, 1000, NULL)")
        db.execSQL("INSERT INTO zikir_history (id, zikirId, amount, type, timestamp, dateKey) VALUES (1, 3, 7, 'add', 1700000000000, '2026-09-01')")

        AppDatabase.MIGRATION_1_6.migrate(db)
        AppDatabase.MIGRATION_6_7.migrate(db)
        AppDatabase.MIGRATION_7_8.migrate(db)

        assertMatchesSchema(db, schema(8))
        assertEquals(1L, count(db, "SELECT COUNT(*) FROM zikir_history WHERE zikirId = 3 AND eventId <> ''"))
        assertEquals(listOf(Triple("zikirId", "zikirs", "CASCADE")), foreignKeys(db, "zikir_history"))
        db.close()
    }

    @Test
    fun migrate7To8_bosTablodaDaUniqueIndexOlusur() {
        val db = createDbAt(7, "mig-7-8-empty.db")
        AppDatabase.MIGRATION_7_8.migrate(db)
        assertMatchesSchema(db, schema(8))
        assertEquals(true, indices(db, "zikir_history")["index_zikir_history_eventId"])
        db.close()
    }

    @Test
    fun roomAcilisi_v7Dosyasini_v8eTasir_veFkCascadeCalisir() {
        // Gercek Room yolu: eski surum dosyasi diskte, uygulama yeni surumle aciyor.
        val name = "room-open-7-to-8.db"
        val legacy = createDbAt(7, name)
        legacy.execSQL("INSERT INTO zikirs (id, target, count, startedAt, completedAt) VALUES (1, 70000, 10, 1000, NULL)")
        legacy.execSQL("INSERT INTO zikirs (id, target, count, startedAt, completedAt) VALUES (2, 70000, 0, NULL, NULL)")
        legacy.execSQL("INSERT INTO zikir_history (id, zikirId, amount, type, timestamp, dateKey) VALUES (1, 1, 10, 'add', 1700000000000, '2026-09-01')")
        legacy.execSQL("INSERT INTO zikir_history (id, zikirId, amount, type, timestamp, dateKey) VALUES (2, 2, 3, 'add', 1700000000000, '2026-09-01')")
        legacy.close()

        val room = androidx.room.Room.databaseBuilder(context, AppDatabase::class.java, name)
            .addMigrations(
                AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3, AppDatabase.MIGRATION_3_4,
                AppDatabase.MIGRATION_4_5, AppDatabase.MIGRATION_5_6, AppDatabase.MIGRATION_1_4,
                AppDatabase.MIGRATION_2_4, AppDatabase.MIGRATION_1_6, AppDatabase.MIGRATION_6_7,
                AppDatabase.MIGRATION_7_8
            )
            .allowMainThreadQueries()
            .build()
        try {
            kotlinx.coroutines.runBlocking {
                val history = room.historyDao().getAllHistoryDirect()
                assertEquals(2, history.size)
                assertTrue(history.none { it.eventId.isBlank() })
                assertEquals(2, history.map { it.eventId }.toSet().size)

                // Room, FK'yi acilista etkinlestirir: repository'nin guvenli UPDATE yolu
                // gecmisi korumali; CASCADE yalnizca gercek silmede tetiklenmeli.
                val repo = com.example.data.repository.ZikirRepository(room)
                repo.updateZikirTarget(1, 90000L)
                assertEquals("updateZikirTarget migration sonrasi gecmisi silmemeli", 2, room.historyDao().getAllHistoryDirect().size)

                room.zikirDao().deleteAll()
                assertEquals("deleteAll CASCADE ile gecmisi bosaltmali", 0, room.historyDao().getHistoryCountDirect())
            }
        } finally {
            room.close()
        }
    }

    @Test
    fun migrate6To8_pendingOperationsTablosuOlusur() {
        val db = createDbAt(6, "mig-6-8.db")
        db.execSQL("INSERT INTO zikirs (id, target, count, startedAt, completedAt) VALUES (3, 500, 7, NULL, NULL)")

        AppDatabase.MIGRATION_6_7.migrate(db)
        AppDatabase.MIGRATION_7_8.migrate(db)

        assertMatchesSchema(db, schema(8))

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
