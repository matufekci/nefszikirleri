package com.example

import com.example.data.backup.PasswordRequiredException
import com.example.data.backup.WrongPasswordException
import com.example.data.cloud.CloudDataCorruptionException
import com.example.data.cloud.NoCloudBackupException
import com.example.data.cloud.SignInCancelledException
import com.example.ui.UiText
import com.example.util.BackupErrorKind
import com.example.util.CloudErrorMapper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Bulut/backup hatalarinin kullaniciya gosterilen mesaja cevrilmesini dogrular.
 *
 * Regresyon 1: `e.localizedMessage` dogrudan gosteriliyordu -> Arapca/Almanca/
 * Fransizca kullaniciya Ingilizce teknik metin ("UNAVAILABLE: Unable to resolve
 * host ...") cikiyordu.
 * Regresyon 2: kullanici Google hesap seciciyi kendi kapattiginda ekranda
 * "Giris islemi iptal edildi" hatasi cikiyordu; artik mesaj gosterilmiyor.
 * Regresyon 3: yerel dosya hatasi (IOException) "internet yok" diye
 * yorumlanmiyordu ve yorumlanmamali.
 */
class CloudErrorMapperTest {

    @Test
    fun `kendi tiplerimiz kesin siniflandirilir`() {
        assertEquals(BackupErrorKind.NO_BACKUP, CloudErrorMapper.classify(NoCloudBackupException()))
        assertEquals(BackupErrorKind.CORRUPT, CloudErrorMapper.classify(CloudDataCorruptionException()))
        assertEquals(BackupErrorKind.PASSWORD_REQUIRED, CloudErrorMapper.classify(PasswordRequiredException()))
        assertEquals(BackupErrorKind.PASSWORD_WRONG, CloudErrorMapper.classify(WrongPasswordException()))
        assertEquals(BackupErrorKind.CANCELLED, CloudErrorMapper.classify(SignInCancelledException()))
    }

    @Test
    fun `Firestore ag ve yetki mesajlari siniflandirilir`() {
        assertEquals(
            BackupErrorKind.NETWORK,
            CloudErrorMapper.classify(Exception("UNAVAILABLE: Unable to resolve host firestore.googleapis.com"))
        )
        assertEquals(
            BackupErrorKind.PERMISSION,
            CloudErrorMapper.classify(Exception("PERMISSION_DENIED: Missing or insufficient permissions"))
        )
    }

    @Test
    fun `zincirin derinindeki sebep bulunur`() {
        val nested = RuntimeException(
            "sarmalayici",
            IllegalStateException("ara", java.net.UnknownHostException("firestore.googleapis.com"))
        )
        assertEquals(BackupErrorKind.NETWORK, CloudErrorMapper.classify(nested))
    }

    @Test
    fun `yerel dosya hatasi ag hatasi SAYILMAZ`() {
        // Bilerek: IOException hem yerel yedek yaziminda hem agda olur.
        // "Internet baglantisi kurulamadi" mesaji yerel hata icin yanlis olur.
        assertEquals(BackupErrorKind.UNKNOWN, CloudErrorMapper.classify(java.io.IOException("disk full")))
        assertEquals(BackupErrorKind.UNKNOWN, CloudErrorMapper.classify(IllegalArgumentException("bozuk dosya")))
    }

    @Test
    fun `dongulu sebep zinciri sonsuz donguye girmez`() {
        // a -> b -> a seklinde gercek bir dongu (Java self-cause'a izin vermez).
        // MAX_CAUSE_HOPS korumasi sayesinde classify sonlu adimda doner.
        val a = Exception("a")
        val b = Exception("b")
        a.initCause(b)
        b.initCause(a)
        assertEquals(BackupErrorKind.UNKNOWN, CloudErrorMapper.classify(a))
    }

    @Test
    fun `sinifsiz hata cagri tarafinin genel mesajina duser`() {
        assertEquals(
            "GENEL",
            CloudErrorMapper.resolve(IllegalStateException("beklenmeyen"), "tr", "GENEL")
        )
        assertEquals("GENEL", CloudErrorMapper.resolve(null, "tr", "GENEL"))
    }

    @Test
    fun `iptal hata olarak isaretlenir ve mesaj uretmez`() {
        assertTrue(CloudErrorMapper.isCancelled(SignInCancelledException()))
        assertFalse(CloudErrorMapper.isCancelled(Exception("UNAVAILABLE: network")))
        // ViewModel bu durumda onResult(false, null) cagirir; metin gosterilmez.
        assertNull(CloudErrorMapper.textFor(BackupErrorKind.UNKNOWN))
    }

    @Test
    fun `her sinifin metni 5 dilde de farklidir`() {
        val langs = listOf("tr", "ar", "en", "de", "fr")
        val kinds = listOf(
            BackupErrorKind.NETWORK,
            BackupErrorKind.PERMISSION,
            BackupErrorKind.NO_BACKUP,
            BackupErrorKind.CORRUPT,
            BackupErrorKind.PASSWORD_REQUIRED,
            BackupErrorKind.PASSWORD_WRONG,
            BackupErrorKind.CANCELLED
        )
        for (kind in kinds) {
            val texts = langs.map { lang ->
                val text = CloudErrorMapper.textFor(kind)?.get(lang)
                assertTrue("Metin yok: $kind/$lang", !text.isNullOrBlank())
                text!!
            }
            // 5 dilin tamami farkli olmali -> bir dil unutulup Turkce'ye dusmedi.
            assertEquals("Dillerden biri digeriyle ayni: $kind", 5, texts.toSet().size)
        }
    }

    @Test
    fun `resolve secilen dili ve L10n fallback overloadunu kullanir`() {
        val error = NoCloudBackupException()
        assertEquals(
            UiText.cloudErrorNoBackup.get("ar"),
            CloudErrorMapper.resolve(error, "ar", "FALLBACK")
        )
        assertEquals(
            UiText.cloudErrorNoBackup.get("de"),
            CloudErrorMapper.resolve(error, "de", UiText.exportStatsError)
        )
        // Sinifsiz hata L10n fallback'ine duser.
        assertEquals(
            UiText.exportStatsError.get("fr"),
            CloudErrorMapper.resolve(Exception("yerel dosya hatasi"), "fr", UiText.exportStatsError)
        )
        assertNotEquals(
            UiText.cloudErrorNoBackup.get("tr"),
            UiText.exportStatsError.get("tr")
        )
    }
}
