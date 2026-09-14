package com.example

import com.example.ui.theme.AppPalettes
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tema kimlikleri ile Firestore kurallarinin tutarliligi.
 *
 * Gecmiste olan hata: `firestore.rules` icinde `settings.themeName in [...]`
 * beyaz listesi vardi ve **pembe tema (`pembe_lux`) o listede yoktu**. Sonuc:
 * pembe temayi secen kullanici "Buluta Yedekle" dediginde Firestore yazmasi
 * PERMISSION_DENIED ile reddediliyordu; hata uygulamada "erisim izni yok"
 * diye gorunuyor, sebebi anlasilmiyordu.
 *
 * Bu test iki seyi kilitler:
 *  1) `AppPalettes.ALL` icindeki her kanonik tema kimligi `normalizeId` ile
 *     kendine donmeli (hicbiri sessizce varsayilana dusmemeli).
 *  2) `firestore.rules` ileride yine bir tema beyaz listesi icerirse, listede
 *     OLMAYAN kanonik tema testi kirmiziya cevirmeli.
 */
class ThemeRulesConsistencyTest {

    private val canonicalIds: List<String> = AppPalettes.ALL.map { it.id }

    @Test
    fun `kanonik tema kimlikleri benzersiz ve kucuk harfli`() {
        assertTrue("Tema listesi bos olmamali", canonicalIds.isNotEmpty())
        assertEquals("Tekrarlanan tema kimligi var", canonicalIds.size, canonicalIds.toSet().size)
        for (id in canonicalIds) {
            assertTrue("Tema kimligi bos: '$id'", id.isNotBlank())
            assertEquals("Tema kimligi kucuk harf olmali: '$id'", id.lowercase(), id)
        }
    }

    @Test
    fun `her kanonik tema normalizeId ile kendine doner`() {
        for (id in canonicalIds) {
            assertEquals(
                "Kanonik tema '$id' normalizeId'de kendine donmedi (varsayilana dusuyor)",
                id,
                AppPalettes.normalizeId(id)
            )
        }
    }

    @Test
    fun `pembe tema takma adlari dogru paleti verir`() {
        // Regresyon: pembe tema rules beyaz listesinde olmadigi icin buluta
        // yazilamiyordu; kimlik eslemesi de bu yuzden onemli.
        assertEquals("pembe_lux", AppPalettes.normalizeId("pembe_lux"))
        assertEquals("pembe_lux", AppPalettes.normalizeId("pembe"))
        assertEquals("pembe_lux", AppPalettes.normalizeId("pink"))
        assertEquals("pembe_lux", AppPalettes.normalizeId("PEMBE_LUX"))
        // Eski takma adlar kanonik ailelere baglanmali.
        assertEquals("hadra_gunduz", AppPalettes.normalizeId("beyaz"))
        assertEquals("siyah", AppPalettes.normalizeId("siyah"))
        // Bilinmeyen kimlik varsayilana duser.
        assertEquals("hadra_gece", AppPalettes.normalizeId("olmayan_tema"))
    }

    @Test
    fun `firestore rules tema beyaz listesi kanonik temalari atlamamali`() {
        val rulesFile = findFirestoreRules()
        if (rulesFile == null) {
            // Depo disindan calistiriliyorsa dosya bulunamaz; Color.kt
            // dogrulamalari yukaridaki testlerde zaten yapildi.
            println("firestore.rules bulunamadi (user.dir=${System.getProperty("user.dir")}); rules kontrolu atlandi")
            return
        }
        val content = rulesFile.readText()
        assertTrue("Yanlis dosya okundu: ${rulesFile.path}", content.contains("isOwner"))

        val marker = "themeName in ["
        val start = content.indexOf(marker)
        if (start < 0) {
            // Istenen durum: beyaz liste yok, dolayisila kayma riski de yok.
            return
        }
        val end = content.indexOf(']', start)
        assertTrue("themeName listesi kapanmiyor", end > start)
        val whitelist = content.substring(start + marker.length, end)
            .split(',')
            .map { it.trim().removeSurrounding("'").removeSurrounding("\"") }
            .filter { it.isNotBlank() }

        val missing = canonicalIds.filter { it !in whitelist }
        assertTrue(
            "firestore.rules tema beyaz listesinde eksik kanonik tema var: $missing " +
                "(bu temalari secen kullanicinin buluta yedeklemesi PERMISSION_DENIED alir)",
            missing.isEmpty()
        )
    }

    private fun findFirestoreRules(): File? {
        var dir: File? = File(System.getProperty("user.dir"))
        var hops = 0
        while (dir != null && hops < 6) {
            val candidate = File(dir, "firestore.rules")
            if (candidate.isFile) return candidate
            dir = dir.parentFile
            hops++
        }
        return null
    }
}
