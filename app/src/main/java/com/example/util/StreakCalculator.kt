package com.example.util

/**
 * Ardisik gün serisi (streak) hesabi.
 *
 * Neden var: `ZikirViewModel` bu hesabi her emission'da (yani her dokunusta)
 * `SimpleDateFormat.parse` + milisaniye bolmesi ile yapiyordu. Iki kusuru vardi:
 *
 * 1) DST HATASI: "yyyy-MM-dd" yerel saatte gece yarisina parse edilip fark
 *    86.400.000'e bolunuyordu. Yaz saati uygulayan bolgelerde (Almanca/Fransizca
 *    kullanicilar: Europe/Berlin, Europe/Paris) bahar gecisinde iki gun arasi
 *    23 saat oldugu icin bolum 0 cikiyor ve seri YANLISLIKLA kopuyordu.
 *    Ornek (Europe/Berlin): 31.03.2024 -> 01.04.2024 = 23 saat -> diffDays 0.
 *    (30->31 Mart kirilmaz; iki gece yarisi da +01:00 oldugu icin 24 saat.)
 * 2) PERFORMANS: komsu her cift icin iki parse; 700 aktif gunde dokunus basina
 *    ~1400 parse.
 *
 * Cozum: takvim gunu sayisi (epoch day) uzerinden tam sayi aritmetigi.
 * Saat dilimi, DST ve locale'den tamamen bagimsiz; `java.time` gerektirmez
 * (minSdk 24, desugaring yok).
 */
object StreakCalculator {

    /**
     * Gregoryen takvimde 1970-01-01'e gore gun sayisi (Howard Hinnant'in
     * days_from_civil algoritmasi). Negatif yillar dahil tam sonuc verir.
     */
    fun daysFromCivil(year: Int, month: Int, day: Int): Long {
        val y = if (month <= 2) year - 1 else year
        val era = (if (y >= 0) y else y - 399) / 400
        val yearOfEra = y - era * 400                       // [0, 399]
        val shiftedMonth = (month + 9) % 12                  // Mar=0 .. Feb=11
        val dayOfYear = (153 * shiftedMonth + 2) / 5 + day - 1   // [0, 365]
        val dayOfEra = yearOfEra * 365 + yearOfEra / 4 - yearOfEra / 100 + dayOfYear
        return era * 146097L + dayOfEra - 719468L
    }

    /** "yyyy-MM-dd" -> gun sayisi; format bozuksa null. */
    fun epochDayOf(dateKey: String): Long? {
        if (dateKey.length != 10 || dateKey[4] != '-' || dateKey[7] != '-') return null
        val year = dateKey.substring(0, 4).toIntOrNull() ?: return null
        val month = dateKey.substring(5, 7).toIntOrNull() ?: return null
        val day = dateKey.substring(8, 10).toIntOrNull() ?: return null
        if (month !in 1..12 || day !in 1..31) return null
        return daysFromCivil(year, month, day)
    }

    /**
     * Verilen "yyyy-MM-dd" listesindeki en uzun ardisik gun serisinin uzunlugu.
     * Liste sirasiz ve/veya tekrarli olabilir; bozuk kayitlar yok sayilir.
     * Bos/gecersiz listede 0 doner.
     */
    fun longestRunDays(dateKeys: List<String>): Int {
        val days = dateKeys.mapNotNull { epochDayOf(it) }.distinct().sorted()
        if (days.isEmpty()) return 0
        var best = 1
        var run = 1
        for (i in 1 until days.size) {
            run = if (days[i] - days[i - 1] == 1L) run + 1 else 1
            if (run > best) best = run
        }
        return best
    }
}
