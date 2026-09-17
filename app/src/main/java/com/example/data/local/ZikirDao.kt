package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Zikir
import kotlinx.coroutines.flow.Flow

@Dao
interface ZikirDao {
    @Query("SELECT * FROM zikirs ORDER BY id ASC")
    fun getAllZikirs(): Flow<List<Zikir>>

    @Query("SELECT * FROM zikirs ORDER BY id ASC")
    suspend fun getAllZikirsDirect(): List<Zikir>

    @Query("SELECT * FROM zikirs WHERE id = :id")
    suspend fun getZikirById(id: Int): Zikir?

    /**
     * YENI satir ekleme (ABORT = Room varsayilani). Ayni id varsa
     * SQLiteConstraintException firlatir; SESSIZCE degistirmez.
     *
     * Neden REPLACE DEGIL: `zikir_history.zikirId -> zikirs.id` iliskisi
     * ON DELETE CASCADE'dir. SQLite'ta `INSERT OR REPLACE` ayni PK'li satiri
     * once SILER sonra yeniden ekler; bu silme CASCADE'i tetikler ve o zikre
     * ait TUM gecmis satirlari sessizce yok olur. Eskiden `updateZikirTarget`
     * ve `fastJumpToZikir` tam olarak bunu yapiyordu (hedef degistirince
     * istatistik/seri kaybi). Artik bu DAO'da REPLACE YOKTUR: mevcut satiri
     * degistirmek icin [update], [updateTarget], [markStarted] veya diger
     * UPDATE sorgulari kullanilir. insertAll yalnizca tablonun BOS oldugu
     * bilinen yerlerde cagrilir: ilk kurulum (`ensureInitialized`, count==0)
     * ve [replaceSnapshot] (once `deleteAll`).
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(zikirs: List<Zikir>)

    /** Bkz. [insertAll]. Mevcut satir icin cagrilirsa hata verir (veri silmez). */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(zikir: Zikir)

    @Query("DELETE FROM zikirs")
    suspend fun deleteAll()

    /**
     * Snapshot geri yukleme: tabloyu bosaltip yeniden doldurur.
     * `deleteAll` CASCADE ile zikir_history'yi de bosaltir; bu, restore
     * akislarinda BILINCLI davranistir (cagiran taraf history'yi ayrica
     * geri yazar). Tek Room transaction icinde calisir; cagiranin
     * `database.withTransaction` blogu varsa ona katilir (nested).
     */
    @androidx.room.Transaction
    suspend fun replaceSnapshot(zikirs: List<Zikir>) {
        deleteAll()
        insertAll(zikirs)
    }

    /**
     * Mevcut zikir satirini yerinde gunceller (UPDATE ... WHERE id = ?).
     * Satiri silmedigi icin CASCADE tetiklenmez; gecmis korunur.
     */
    @Update
    suspend fun update(zikir: Zikir)

    /**
     * Yalnizca hedefi ve tamamlanma damgasini gunceller. Sayac ve startedAt
     * dokunulmaz. Satir silinmez -> history korunur.
     */
    @Query("UPDATE zikirs SET target = :target, completedAt = :completedAt WHERE id = :id")
    suspend fun updateTarget(id: Int, target: Long, completedAt: Long?)

    /**
     * Zikri "baslamis" olarak isaretler; startedAt zaten doluysa dokunmaz.
     * Satir silinmez -> history korunur.
     */
    @Query("UPDATE zikirs SET startedAt = :now WHERE id = :id AND (startedAt IS NULL OR startedAt = 0)")
    suspend fun markStarted(id: Int, now: Long)

    @Query("UPDATE zikirs SET count = count + :amount, startedAt = CASE WHEN startedAt IS NULL OR startedAt = 0 THEN :now ELSE startedAt END, completedAt = CASE WHEN count + :amount >= target THEN COALESCE(completedAt, :now) ELSE NULL END WHERE id = :id")
    suspend fun incrementZikirCount(id: Int, amount: Long, now: Long)

    @Query("UPDATE zikirs SET count = MAX(0, count - :amount), completedAt = CASE WHEN MAX(0, count - :amount) < target THEN NULL ELSE completedAt END WHERE id = :id")
    suspend fun decrementZikirCount(id: Int, amount: Long)

    @Query("UPDATE zikirs SET count = 0, startedAt = NULL, completedAt = NULL WHERE id = :id")
    suspend fun resetZikir(id: Int)

    @Query("UPDATE zikirs SET count = 0, startedAt = NULL, completedAt = NULL")
    suspend fun resetAllZikirs()

    @Query("SELECT COUNT(*) FROM zikirs")
    suspend fun getCount(): Int
}
