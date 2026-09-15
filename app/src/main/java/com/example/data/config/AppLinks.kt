package com.example.data.config

/**
 * Uygulama disina acilan SABIT web adreslerinin TEK merkezi kaynagi.
 *
 * Neden var: Google Play iki dis kaynak istiyor —
 *  1) gecerli bir GIZLILIK POLITIKASI (store listing + uygulama ici),
 *  2) hesap olusturan uygulamalarda harici bir HESAP SILME web kaynagi.
 * Bu adresler onceki surumlerde kodda hic yoktu; uygulama icinde erisim
 * noktasi da bulunmuyordu.
 *
 * KURAL: Buraya ASLA uydurma/ornek adres yazilmaz. Degerler bos birakildi;
 * gercek adresler sahibi tarafindan doldurulacak. [isUsable] bos veya
 * gelistirme/placeholder adresleri "kullanilamaz" sayar, boylece yanlislikla
 * `https://example.com/...` gibi bir adres uretime sizarsa kullaniciya
 * actirilmaz (sessizce yanlis sayfaya gitmek yerine kullaniciya mesaj gosterilir).
 *
 * NOT: Play Console'a girilecek URL ile buradaki URL ayni gercek endpoint'e
 * baglanmalidir; iki ayri farkli adres beyan tutarsizligi yaratir.
 */
object AppLinks {

    /**
     * Gizlilik politikasi adresi.
     * TODO(sahibi): gercek production adresi ile doldur (https:// ile baslamali).
     */
    const val PRIVACY_POLICY_URL: String = ""

    /**
     * Harici hesap silme talebi adresi (Play Console "Data deletion" alani).
     * TODO(sahibi): gercek production adresi ile doldur (https:// ile baslamali).
     */
    const val ACCOUNT_DELETION_URL: String = ""

    /** Uretimde gercekten acilabilir bir adres mi? */
    fun isUsable(url: String): Boolean {
        val trimmed = url.trim()
        if (trimmed.isEmpty()) return false
        if (!trimmed.startsWith("https://", ignoreCase = true)) return false
        val forbidden = listOf(
            "example.com",
            "example.org",
            "example.net",
            "localhost",
            "127.0.0.1",
            "10.0.2.2",
            "todo",
            "replace",
            "placeholder",
            "yourdomain",
            "your-domain",
            "site.com"
        )
        val lower = trimmed.lowercase()
        return forbidden.none { lower.contains(it) }
    }
}
