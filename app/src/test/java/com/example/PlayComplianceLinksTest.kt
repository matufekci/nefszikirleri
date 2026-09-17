package com.example

import com.example.data.cloud.SignInErrorKind
import com.example.data.config.AppLinks
import com.example.ui.UiText
import com.example.util.CloudErrorMapper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * PLAY UYUMLULUGU (Prompt 11.1) bekcisi.
 *
 * Iki gercek acik vardi: uygulama icinde gizlilik politikasi erisimi yoktu ve
 * hesap olusturan uygulamada hesap+veri silme yolu yoktu. Bu test, duzeltmenin
 * SESSIZCE bozulmasini engeller:
 *  - koda uydurma/ornek (example.com, localhost, http) bir adres sizarsa,
 *  - placeholder adres "acilabilir" sayilirsa,
 *  - yeni kullanici metinlerinden biri 5 dilden birinde eksik/kopya olursa,
 *  - re-auth hatasi kullaniciya cevrilemez hale gelirse
 * test kirmizi yanar.
 *
 * Robolectric gerektirmez: yalnizca saf Kotlin siniflari yuklenir.
 */
class PlayComplianceLinksTest {

    private companion object {
        val LANGS = listOf("tr", "ar", "en", "de", "fr")
    }

    @Test
    fun `placeholder ve ornek adresler kullanilamaz sayilir`() {
        assertFalse("Bos adres acilabilir sayilmamali", AppLinks.isUsable(""))
        assertFalse("Bosluk adres acilabilir sayilmamali", AppLinks.isUsable("   "))
        assertFalse("Ornek alan adi reddedilmeli", AppLinks.isUsable("https://example.com/privacy"))
        assertFalse("localhost reddedilmeli", AppLinks.isUsable("https://localhost/privacy"))
        assertFalse("Emulator loopback reddedilmeli", AppLinks.isUsable("https://10.0.2.2/privacy"))
        assertFalse("Guvenli olmayan http reddedilmeli", AppLinks.isUsable("http://nefszikirleri.app/privacy"))
        assertFalse("TODO placeholder reddedilmeli", AppLinks.isUsable("https://todo-replace.app/privacy"))
        assertTrue("Gercek https adres kabul edilmeli", AppLinks.isUsable("https://nefszikirleri.app/privacy"))
        assertTrue("Buyuk/kucuk harf duyarsiz olmali", AppLinks.isUsable("HTTPS://nefszikirleri.app/privacy"))
    }

    @Test
    fun `koda sahte production URL sizmamistir`() {
        // Gercek adresler sahibi tarafindan doldurulana kadar bos kalir.
        // Buraya ornek/uydurma bir adres yazilirsa asagidaki isUsable kontrolu
        // onu kabul etmez ve test kirmizi yanar.
        val privacy = AppLinks.PRIVACY_POLICY_URL
        val deletion = AppLinks.ACCOUNT_DELETION_URL
        assertTrue(
            "PRIVACY_POLICY_URL ya bos ya da gercek https adres olmali: '$privacy'",
            privacy.isBlank() || AppLinks.isUsable(privacy)
        )
        assertTrue(
            "ACCOUNT_DELETION_URL ya bos ya da gercek https adres olmali: '$deletion'",
            deletion.isBlank() || AppLinks.isUsable(deletion)
        )
        assertFalse("Ornek alan adi koda sizmis", privacy.contains("example.", ignoreCase = true))
        assertFalse("Ornek alan adi koda sizmis", deletion.contains("example.", ignoreCase = true))
    }

    @Test
    fun `yeni Play metinleri 5 dilde de dolu ve birbirinden farkli`() {
        val entries = mapOf(
            "privacyPolicy" to UiText.privacyPolicy,
            "privacyPolicyUnavailable" to UiText.privacyPolicyUnavailable,
            "openLinkFailed" to UiText.openLinkFailed,
            "deleteAccount" to UiText.deleteAccount,
            "deleteAccountConfirmTitle" to UiText.deleteAccountConfirmTitle,
            "deleteAccountConfirmMsg" to UiText.deleteAccountConfirmMsg,
            "deleteAccountConfirmBtn" to UiText.deleteAccountConfirmBtn,
            "deleteAccountSuccess" to UiText.deleteAccountSuccess,
            "deleteAccountFailed" to UiText.deleteAccountFailed,
            "deleteAccountCloudOnlyDeleted" to UiText.deleteAccountCloudOnlyDeleted,
            "accountDeletionReauthRequired" to UiText.accountDeletionReauthRequired
        )
        for ((name, l10n) in entries) {
            val texts = LANGS.map { l10n.get(it) }
            texts.forEachIndexed { index, text ->
                assertTrue("$name -> ${LANGS[index]} bos", text.isNotBlank())
            }
            assertEquals(
                "$name -> iki dilde ayni metin var (kopya ceviri): $texts",
                LANGS.size,
                texts.toSet().size
            )
        }
    }

    @Test
    fun `re-auth hatasi kullaniciya 5 dilde anlatilabiliyor`() {
        val text = CloudErrorMapper.textForSignIn(SignInErrorKind.REAUTH_REQUIRED)
        assertNotNull("REAUTH_REQUIRED icin kullanici metni yok", text)
        val texts = LANGS.map { text!!.get(it) }
        texts.forEach { assertTrue("reauth metni bos", it.isNotBlank()) }
        assertEquals("reauth metni dillerde kopyalanmis", LANGS.size, texts.toSet().size)
    }
}
