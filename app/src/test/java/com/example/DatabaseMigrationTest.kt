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
