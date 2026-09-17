package com.example

import com.example.ui.L10n
import com.example.ui.UiText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * BES DIL KURALININ otomatik bekçisi.
 *
 * Kural: kullaniciya gorunen her metin 5 dilde (tr, ar, en, de, fr) olmak
 * zorunda. `L10n` constructor'i 5 parametreyi zorunlu tuttugu icin bir dili
 * UNUTMAK zaten derleme hatasi; ama ayni metni iki dile KOPYALAMAK (orn.
 * Arapca yerine Turkce yazmak) derleyiciden geciyordu. Bu test onu yakalar:
 * `UiText` icindeki her `L10n` alani 5 dilde de dolu ve birbirinden farkli
 * olmali.
 *
 * Reflection kullanilmasi bilerek: yeni bir metin ekleyen kisinin testi
 * guncellemesine gerek kalmasin, kapsamin kendisi otomatik denetlensin.
 */
class L10nCoverageTest {

    private companion object {
        val LANGS = listOf("tr", "ar", "en", "de", "fr")

        /**
         * Reflection kirilirsa (orn. UiText baska objelere bolunurse) alan
         * sayisi sifira duser ve test sessizce "her sey tamam" der. Bu esik
         * o durumu yakalar. Su an 51 girdi var; esik bilerek biraz altta
         * tutuldu ki mesru metin silmeleri yanlis alarm uretmesin.
         */
        const val MIN_ENTRY_COUNT = 45
    }

    private fun uiTextEntries(): List<Pair<String, L10n>> {
        val instance = UiText::class.java.getDeclaredField("INSTANCE")
            .apply { isAccessible = true }
            .get(null)
        return UiText::class.java.declaredFields
            .filter { it.type == L10n::class.java }
            .map { field ->
                field.isAccessible = true
                field.name to (field.get(instance) as L10n)
            }
    }

    @Test
    fun `UiText icindeki her metin 5 dilde de dolu ve farkli`() {
        val entries = uiTextEntries()
        assertTrue(
            "UiText girdi sayisi beklenenden az (${entries.size}); reflection kirilmis olabilir",
            entries.size >= MIN_ENTRY_COUNT
        )
        for ((name, l10n) in entries) {
            val texts = LANGS.map { lang -> l10n.get(lang) }
            for ((index, text) in texts.withIndex()) {
                assertTrue(
                    "$name -> ${LANGS[index]} bos",
                    text.isNotBlank()
                )
            }
            assertEquals(
                "$name -> iki dilde ayni metin var (kopyalanmis ceviriler): $texts",
                LANGS.size,
                texts.toSet().size
            )
        }
    }

    @Test
    fun `L10n get dili dogru secer ve bilinmeyen dilde Turkceye duser`() {
        val sample = UiText.cloudErrorNetwork
        // Her dil kendi metnini vermeli (hicbiri digerinin kopyasi degil).
        val texts = LANGS.map { sample.get(it) }.toSet()
        assertEquals("L10n.get yanlis dili seciyor", LANGS.size, texts.size)
        // Buyuk/kucuk harf duyarsiz secim.
        assertEquals(sample.get("fr"), sample.get("FR"))
        assertEquals(sample.get("en"), sample.get("En"))
        // Bilinmeyen/bos dil -> Turkce (kullanicinin anlamadigi bir dil gosterilmez).
        assertEquals(sample.get("tr"), sample.get("xx"))
        assertEquals(sample.get("tr"), sample.get(""))
    }

    @Test
    fun `format yer tutuculari sirayla doldurur`() {
        val formatted = UiText.lastSyncAt.format("en", "12:30")
        assertTrue("format calismadi: $formatted", formatted.contains("12:30"))
        assertTrue("yer tutucu kaldirilmadi: $formatted", !formatted.contains("{0}"))
    }
}
