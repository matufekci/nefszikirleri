package com.example.util

/**
 * Gunluk vird hatirlatmasi icin manevi, davetkar bildirim metinleri.
 *
 * Neden var: Bildirim eskiden "Nefs Zikirleri - Gunluk virdiniz" gibi duz bir
 * metinle cikiyordu; kullanicinin bildirime tiklayip zikre baslama istegi
 * uyandirmiyordu. Burada her dil icin ayet/hadis referansli kisa mesajlar
 * donusumlu olarak sunuluyor.
 *
 * Dil guvencesi: [pick5] bes dili de parametre olarak ZORUNLU kildigi icin bir
 * dil listesi eksik olsa kod derlenmez; kullaniciya sessizce baska dil
 * gosterilemez.
 */
object DailyReminderMessages {

    private data class Msg(val title: String, val body: String)

    /** Bes listeden birini secmek icin; hepsini gecmek zorunlu. */
    private fun <T> pick5(lang: String, tr: T, ar: T, en: T, de: T, fr: T): T =
        when (lang.lowercase()) {
            "ar" -> ar
            "en" -> en
            "de" -> de
            "fr" -> fr
            else -> tr
        }

    private val TR = listOf(
        Msg(
            "Kalpler ancak zikirle huzur bulur",
            "\"Biliniz ki kalpler ancak Allah'ı anmakla huzur bulur.\" (Ra'd, 28) — Bugünkü virdine birkaç dakika ayır."
        ),
        Msg(
            "Bir tesbih, bir adım daha yakın",
            "Efendimiz ﷺ buyurdu: \"Kim sübhânallâhi ve bihamdihî derse, günahları deniz köpüğü kadar olsa da bağışlanır.\" Tesbihini eline al."
        ),
        Msg(
            "Bugün kalbini dinlendir",
            "Bu vakit zikir için en bereketli anlardan. Kısa bir vird, günün tamamına sükûnet yayar."
        ),
        Msg(
            "Beni anın ki ben de sizi anayım",
            "\"Öyleyse beni anın ki ben de sizi anayım.\" (Bakara, 152) — Birkaç dakikalık zikir, kalbin yükünü hafifletir."
        ),
        Msg(
            "Dilin zikirle ıslak kalsın",
            "\"Amellerin en faziletlisi, dilin Allah'ın zikriyle ıslak olmasıdır.\" Bugünkü virdini tamamlamaya var mısın?"
        ),
        Msg(
            "Gün bitmeden bir güzellik daha",
            "Zikir dile kolay, terazide ağırdır. Bugünkü payını tamamla, kalbin ferahlasın."
        )
    )

    private val AR = listOf(
        Msg(
            "ألا بذكر الله تطمئن القلوب",
            "\"الذين آمنوا وتطمئن قلوبهم بذكر الله\" (الرعد 28) — اجعل لوردك اليوم نصيباً من وقتك."
        ),
        Msg(
            "تسبيحةٌ تقرّبك",
            "قال ﷺ: «من قال سبحان الله وبحمده غُفرت خطاياه ولو كانت مثل زبد البحر». خذ سبحتك واذكر الله."
        ),
        Msg(
            "دع قلبك يستريح",
            "هذا الوقت من أنفع أوقات الذكر؛ وردٌ قصير يملأ يومك سكينة."
        ),
        Msg(
            "اذكروني أذكركم",
            "\"فاذكروني أذكركم\" (البقرة 152) — دقائق من الذكر تُخفّف عن القلب حِمله."
        ),
        Msg(
            "لا يزال لسانك رطباً",
            "«أفضل الأعمال أن تموت ولسانك رطب من ذكر الله». أتكمل وردك اليوم؟"
        ),
        Msg(
            "قبل أن ينتهي اليوم",
            "الذكر خفيف على اللسان ثقيل في الميزان؛ أتمم نصيبك اليوم فيفرح قلبك."
        )
    )

    private val EN = listOf(
        Msg(
            "Hearts find rest in remembrance",
            "\"Verily, in the remembrance of Allah do hearts find rest.\" (Ar-Ra'd 28) — Spare a few minutes for today's dhikr."
        ),
        Msg(
            "One tasbih, one step closer",
            "The Prophet ﷺ said: \"Whoever says SubhanAllahi wa bihamdih, their sins are forgiven even if they were like the foam of the sea.\""
        ),
        Msg(
            "Let your heart breathe",
            "This is among the most blessed times for dhikr. A short wird spreads calm across the whole day."
        ),
        Msg(
            "Remember Me, and I will remember you",
            "\"So remember Me; I will remember you.\" (Al-Baqarah 152) — A few minutes of dhikr lightens the heart."
        ),
        Msg(
            "Keep your tongue moist with dhikr",
            "\"The best of deeds is that your tongue stays moist with the remembrance of Allah.\" Will you complete today's wird?"
        ),
        Msg(
            "Before the day slips away",
            "Dhikr is light on the tongue yet heavy on the scale. Complete today's portion and let your heart be at ease."
        )
    )

    private val DE = listOf(
        Msg(
            "Herzen finden Ruhe im Gedenken",
            "\"Wahrlich, im Gedenken Allahs finden die Herzen Ruhe.\" (Ar-Ra'd 28) — Nimm dir heute einige Minuten für deinen Dhikr."
        ),
        Msg(
            "Ein Tasbih, ein Schritt näher",
            "Der Prophet ﷺ sagte: \"Wer SubhanAllahi wa bihamdih sagt, dem werden die Sünden vergeben, auch wenn sie wie der Schaum des Meeres wären.\""
        ),
        Msg(
            "Lass dein Herz aufatmen",
            "Dies ist eine der gesegnetsten Zeiten für Dhikr. Ein kurzer Wird bringt Ruhe in den ganzen Tag."
        ),
        Msg(
            "Gedenkt Meiner, so gedenke Ich euer",
            "\"So gedenkt Meiner, dann gedenke Ich euer.\" (Al-Baqara 152) — Einige Minuten Dhikr erleichtern das Herz."
        ),
        Msg(
            "Halte deine Zunge feucht",
            "\"Die beste Tat ist, dass deine Zunge feucht bleibt vom Gedenken Allahs.\" Vollendest du heute deinen Wird?"
        ),
        Msg(
            "Bevor der Tag vergeht",
            "Dhikr ist leicht auf der Zunge, doch schwer auf der Waagschale. Vollende heute deinen Anteil."
        )
    )

    private val FR = listOf(
        Msg(
            "Les cœurs s'apaisent dans le rappel",
            "\"N'est-ce point par l'évocation d'Allah que se tranquillisent les cœurs ?\" (Ar-Ra'd 28) — Accorde quelques minutes à ton dhikr aujourd'hui."
        ),
        Msg(
            "Un tasbih, un pas de plus",
            "Le Prophète ﷺ a dit : « Quiconque dit SubhanAllahi wa bihamdih, ses péchés sont pardonnés fussent-ils comme l'écume de la mer. »"
        ),
        Msg(
            "Laisse ton cœur respirer",
            "C'est l'un des moments les plus bénis pour le dhikr. Un court wird apaise toute la journée."
        ),
        Msg(
            "Évoquez-Moi, Je vous évoquerai",
            "\"Évoquez-Moi, Je vous évoquerai.\" (Al-Baqara 152) — Quelques minutes de dhikr allègent le cœur."
        ),
        Msg(
            "Garde ta langue humide de rappel",
            "« La meilleure des œuvres est que ta langue demeure humide du rappel d'Allah. » Accomplis-tu ton wird aujourd'hui ?"
        ),
        Msg(
            "Avant que le jour ne s'achève",
            "Le dhikr est léger sur la langue mais lourd dans la balance. Accomplis ta part aujourd'hui."
        )
    )

    /**
     * [seed] degerine gore donusumlu mesaj secer (orn. yilin gunu + slot kodu),
     * boylece ayni gun farkli saat dilimleri farkli mesaj gosterir ve mesajlar
     * her gun degisir. Dönen indeks oldugu icin sinir asimi imkansiz.
     */
    fun pick(lang: String, seed: Int): Pair<String, String> {
        val pool = pick5(lang, TR, AR, EN, DE, FR)
        val msg = pool[((seed % pool.size) + pool.size) % pool.size]
        return msg.title to msg.body
    }
}
