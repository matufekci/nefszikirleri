package com.example.util

import android.content.Context
import androidx.core.content.edit

/**
 * Akşam hedef hatırlatması ve "son viraj" motivasyon bildirimleri için
 * diller arası, birbirinden bağımsız metin havuzları.
 *
 * Her gönderimde rastgele farklı bir şablon seçilir; aynı şablonun üst üste
 * gelmesi önlenir (son indeks SharedPreferences'ta tutulur). "Son viraj"
 * şablonları %d içerir — kalan zikir sayısı yerleştirilir.
 */
object EveningReminderMessages {

    private const val PREFS = "evening_reminder_prefs"
    private const val KEY_LAST_EVENING = "last_evening_idx"
    private const val KEY_LAST_FINISH = "last_finish_idx"

    private val eveningTr = listOf(
        "Akşam duası vakti: bugünkü virdin hâlâ seni bekliyor.",
        "Günün yorgunluğunu zikirle dinlenmeye bırak — birkaç dakika ayır.",
        "Akşam serinliğinde dilin zikirle nurlansın.",
        "Bugünkü hedefin için hâlâ vakit var; bir tesbihle başla.",
        "Melekler akşama da şahitlik eder; virdini tamamla.",
        "Akşamın sessizliğinde bir zikir halkası daha."
    )
    private val eveningAr = listOf(
        "حان وقت ذكر المساء: ورد اليوم ما زال ينتظرك.",
        "بدّل تعب النهار بسكينة الذكر — دقائق قليلة تكفي.",
        "لتستضئ لسانك بالذكر في هدوء المساء.",
        "ما زال الوقت متاحاً لهدف اليوم — ابدأ بمسبحة واحدة.",
        "الملائكة تشهد المساء أيضاً؛ أتمّ وردك.",
        "حلقة ذكر أخرى في سكون المساء."
    )
    private val eveningEn = listOf(
        "Evening remembrance time: today's wird is still waiting for you.",
        "Trade the day's fatigue for rest in remembrance — a few minutes will do.",
        "Let your tongue be lit with dhikr in the evening calm.",
        "There is still time for today's target — start with one tasbih.",
        "The angels witness the evening too; complete your wird.",
        "One more circle of remembrance in the quiet of the evening."
    )
    private val eveningDe = listOf(
        "Zeit des Abendgedenkens: dein heutiger Wird wartet noch auf dich.",
        "Tausche die Müdigkeit des Tages gegen Ruhe im Dhikr — wenige Minuten genügen.",
        "Lass deine Zunge in der Abendruhe mit Dhikr erleuchtet sein.",
        "Für das heutige Ziel ist noch Zeit — beginn mit einem Tasbih.",
        "Auch am Abend bezeugen die Engel; vollende deinen Wird.",
        "Noch ein Kreis des Gedenkens in der Stille des Abends."
    )
    private val eveningFr = listOf(
        "L'heure du souvenir du soir : le wird du jour t'attend encore.",
        "Échange la fatigue du jour contre le repos du dhikr — quelques minutes suffisent.",
        "Que ta langue s'illumine de dhikr dans le calme du soir.",
        "Il est encore temps pour l'objectif du jour — commence par un tasbih.",
        "Les anges sont témoins aussi le soir ; accomplis ton wird.",
        "Encore un cercle de souvenir dans le silence du soir."
    )

    private val finishTr = listOf(
        "Son virajdasın: sadece %d zikir kaldı; bu kadar yaklaşmışken bırakma.",
        "Hedefin %d zikir uzağında — birkaç dakikada günü tamamlayabilirsin.",
        "Bu kadar yol geldin; kalan %d zikiri de nurlandır.",
        "Gecenin kapısında %d zikir seni bekliyor; tamamla, rahat uyu.",
        "%d zikir kaldı — az bir gayretle bugünün mührünü vur.",
        "Neredeyse bitti: %d zikir sonra hedef tamam. Devam!"
    )
    private val finishAr = listOf(
        "المنعطف الأخير: بقي %d ذكر فقط؛ لا تتوقف وقد اقتربت لهذا الحد.",
        "هدفك على بعد %d ذكر — يمكنك إتمام اليوم في دقائق.",
        "وصلت إلى هنا؛ أنر ما تبقى من %d ذكر أيضاً.",
        "عند باب الليل ينتظرك %d ذكر؛ أتمم ونم قرير العين.",
        "بقي %d ذكر — بجهد يسير يُختم اليوم.",
        "اقتربت تماماً: بعد %d ذكر يكتمل الهدف. واصل!"
    )
    private val finishEn = listOf(
        "Final stretch: only %d dhikr left — don't stop this close to the target.",
        "Your target is %d dhikr away — you can complete the day in minutes.",
        "You have come this far; illuminate the remaining %d dhikr too.",
        "At the door of the night, %d dhikr await; finish and rest easy.",
        "%d dhikr left — a little effort seals the day.",
        "Almost done: %d dhikr and the target is complete. Keep going!"
    )
    private val finishDe = listOf(
        "Zielgerade: nur noch %d Dhikr — hör nicht so kurz vor dem Ziel auf.",
        "Dein Ziel ist %d Dhikr entfernt — in Minuten ist der Tag vollständig.",
        "So weit gekommen; erleuchte auch die verbleibenden %d Dhikr.",
        "An der Schwelle der Nacht warten %d Dhikr; vollende und ruhe sanft.",
        "%d Dhikr übrig — mit etwas Mühe ist das Tagessiegel gesetzt.",
        "Fast geschafft: %d Dhikr und das Ziel ist voll. Weiter!"
    )
    private val finishFr = listOf(
        "Dernière ligne droite : plus que %d dhikr — ne t'arrête pas si près du but.",
        "Ton objectif est à %d dhikr — le jour peut être complet en quelques minutes.",
        "Tu es arrivé si loin ; illumine aussi les %d dhikr restants.",
        "À la porte de la nuit, %d dhikr t'attendent ; termine et dors tranquille.",
        "%d dhikr restants — un petit effort scelle la journée.",
        "Presque fini : %d dhikr et l'objectif est atteint. Continue !"
    )

    private fun pool(lang: String, type: Type): List<String> = when (type) {
        Type.EVENING -> when (lang) {
            "ar" -> eveningAr; "en" -> eveningEn; "de" -> eveningDe; "fr" -> eveningFr
            else -> eveningTr
        }
        Type.FINISH -> when (lang) {
            "ar" -> finishAr; "en" -> finishEn; "de" -> finishDe; "fr" -> finishFr
            else -> finishTr
        }
    }

    enum class Type { EVENING, FINISH }

    /** Rastgele ama üst üste tekrarsız şablon seçer. */
    fun pick(context: Context, lang: String, type: Type): String {
        val list = pool(lang, type)
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val key = if (type == Type.EVENING) KEY_LAST_EVENING else KEY_LAST_FINISH
        val last = prefs.getInt(key, -1)
        var idx = (Math.random() * list.size).toInt()
        if (list.size > 1 && idx == last) idx = (idx + 1) % list.size
        prefs.edit { putInt(key, idx) }
        return list[idx]
    }
}
