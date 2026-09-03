package com.example.data.model

data class Badge(
    val id: String,
    val title: String,
    val desc: String,
    val icon: String,
    val isUnlocked: Boolean,
    val category: String = "terkip", // "terkip" or "istikrar"
    val requirement: String = "",
    val currentProgress: Long = 0L,
    val targetProgress: Long = 1L,
    val progressPercent: Int = 0
)

object BadgeManager {
    fun getAllBadges(
        totalDone: Long,
        completedCount: Int,
        bestStreak: Int,
        lang: String = "tr"
    ): List<Badge> {
        val l = lang.lowercase()
        val strings = AppStrings.get(l)

        // Helper to compute progress percent clamped between 0 and 100
        fun calcPercent(curr: Long, target: Long): Int {
            if (target <= 0L) return 100
            return ((curr.toDouble() / target.toDouble()) * 100).toInt().coerceIn(0, 100)
        }

        return listOf(
            // 1. 1. Zikir Tamamlanması (100.000 Hedefine Ulaşma)
            Badge(
                id = "zikir_1",
                title = when (l) {
                    "ar" -> "إكمال المرتبة الأولى"
                    "en" -> "1st Level Completed"
                    "de" -> "1. Stufe Abgeschlossen"
                    "fr" -> "1er Niveau Complété"
                    else -> "1. Zikir Tamamlanması"
                },
                desc = when (l) {
                    "ar" -> "تم إنجاز المرتبة الأولى (كلمة التوحيد - ١٠٠ ألف تسبيحة)."
                    "en" -> "Completed 1st Level (Kelime-i Tawhid - 100,000 dhikr)."
                    "de" -> "1. Stufe erfolgreich beendet."
                    "fr" -> "1er niveau accompli avec succès."
                    else -> "1. Zikir tamamlandı."
                },
                icon = "🌱",
                isUnlocked = completedCount >= 1,
                category = "terkip",
                requirement = when (l) {
                    "ar" -> "إتمام المرتبة الأولى (١٠٠ ألف ذكر)"
                    "en" -> "Complete Level 1 (100,000 dhikr)"
                    "de" -> "Stufe 1 abschließen"
                    "fr" -> "Compléter le Niveau 1 (100 000 dhikrs)"
                    else -> "1. Zikri tamamlamak"
                },
                currentProgress = completedCount.toLong().coerceAtMost(1L),
                targetProgress = 1L,
                progressPercent = if (completedCount >= 1) 100 else calcPercent(totalDone.coerceAtMost(100000L), 100000L)
            ),
            // 3. 3 Zikir Tamamlanması
            Badge(
                id = "zikir_3",
                title = when (l) {
                    "ar" -> "محطة المراتب الثلاث"
                    "en" -> "3 Levels Milestone"
                    "de" -> "Drei-Stufen-Meilenstein"
                    "fr" -> "Étape des 3 Niveaux"
                    else -> "Üç Zikir Bendi"
                },
                desc = when (l) {
                    "ar" -> "تم إكمال الأوراد للـ ٣ مراتب الأولى (٣٠٠ ألف ذكر)."
                    "en" -> "Completed the first 3 levels (300,000 dhikr total)."
                    "de" -> "Die ersten 3 Stufen wurden vollendet."
                    "fr" -> "Les 3 premiers niveaux (300 000 dhikrs) sont complétés."
                    else -> "İlk 3 zikrin virdi ikmal edildi."
                },
                icon = "🌿",
                isUnlocked = completedCount >= 3,
                category = "terkip",
                requirement = when (l) {
                    "ar" -> "إتمام ٣ مراتب كاملة من الأذكار الـ ١٥"
                    "en" -> "Complete 3 full levels"
                    "de" -> "3 vollständige Stufen abschließen"
                    "fr" -> "Compléter 3 niveaux complets"
                    else -> "3 tam zikri ikmal etmek"
                },
                currentProgress = completedCount.toLong().coerceAtMost(3L),
                targetProgress = 3L,
                progressPercent = calcPercent(completedCount.toLong(), 3L)
            ),
            // 4. 5 Zikir Tamamlanması (Yolun Üçte Biri)
            Badge(
                id = "one_third",
                title = when (l) {
                    "ar" -> "ثلث الطريق"
                    "en" -> "One Third of Path"
                    "de" -> "Ein Drittel des Weges"
                    "fr" -> "Un Tiers du Chemin"
                    else -> "Yolun Üçte Biri"
                },
                desc = when (l) {
                    "ar" -> "تم إكمال ٥ مراتب من التركيب (٥٠٠ ألف ذكر)."
                    "en" -> "Completed 5 full Terkip levels (500,000 dhikr)."
                    "de" -> "5 Terkip-Stufen wurden gemeistert."
                    "fr" -> "5 niveaux complets (500 000 dhikrs) accomplis."
                    else -> "5 Zikir tamamlandı."
                },
                icon = "⭐",
                isUnlocked = completedCount >= 5,
                category = "terkip",
                requirement = when (l) {
                    "ar" -> "إتمام ٥ مراتب كاملة (٥٠٠ ألف ذكر)"
                    "en" -> "Complete 5 levels (500,000 dhikr)"
                    "de" -> "5 Stufen abschließen"
                    "fr" -> "Compléter 5 niveaux (500 000 dhikrs)"
                    else -> "5 zikri tamamlamak"
                },
                currentProgress = completedCount.toLong().coerceAtMost(5L),
                targetProgress = 5L,
                progressPercent = calcPercent(completedCount.toLong(), 5L)
            ),
            // 5. 8 Zikir Tamamlanması (Yolun Yarısı)
            Badge(
                id = "half_way",
                title = when (l) {
                    "ar" -> "نصف الطريق"
                    "en" -> "Half Way Milestone"
                    "de" -> "Halbzeit auf dem Pfad"
                    "fr" -> "Moitié du Chemin"
                    else -> "Yolun Yarısı"
                },
                desc = when (l) {
                    "ar" -> "تم إكمال ٨ مراتب من أوراد التركيب المباركة."
                    "en" -> "Completed 8 Terkip levels along the spiritual ascent."
                    "de" -> "8 Terkip-Stufen wurden erfolgreich abgeschlossen."
                    "fr" -> "8 niveaux du parcours spirituel accomplis."
                    else -> "8 Zikir ikmal edildi."
                },
                icon = "🌕",
                isUnlocked = completedCount >= 8,
                category = "terkip",
                requirement = when (l) {
                    "ar" -> "إتمام ٨ مراتب كاملة"
                    "en" -> "Complete 8 levels"
                    "de" -> "8 Stufen abschließen"
                    "fr" -> "Compléter 8 niveaux"
                    else -> "8 zikri tamamlamak"
                },
                currentProgress = completedCount.toLong().coerceAtMost(8L),
                targetProgress = 8L,
                progressPercent = calcPercent(completedCount.toLong(), 8L)
            ),
            // 6. 10 Zikir Tamamlanması
            Badge(
                id = "zikir_10",
                title = when (l) {
                    "ar" -> "كمال عشر مراتب"
                    "en" -> "10 Levels Milestone"
                    "de" -> "Zehn-Stufen-Reife"
                    "fr" -> "Perfection des 10 Niveaux"
                    else -> "On Zikir Kemali"
                },
                desc = when (l) {
                    "ar" -> "تم إكمال ١٠ مراتب."
                    "en" -> "Reached 10 completed levels."
                    "de" -> "10 Stufen wurden vollendet."
                    "fr" -> "10 niveaux accomplis."
                    else -> "10 Zikir tamamlandı."
                },
                icon = "👑",
                isUnlocked = completedCount >= 10,
                category = "terkip",
                requirement = when (l) {
                    "ar" -> "إتمام ١٠ مراتب كاملة"
                    "en" -> "Complete 10 levels"
                    "de" -> "10 Stufen abschließen"
                    "fr" -> "Compléter 10 niveaux"
                    else -> "10 zikri tamamlamak"
                },
                currentProgress = completedCount.toLong().coerceAtMost(10L),
                targetProgress = 10L,
                progressPercent = calcPercent(completedCount.toLong(), 10L)
            ),
            // 7. Tam Zikir Hatmi (15 Zikir)
            Badge(
                id = "terkip_hatmi",
                title = when (l) {
                    "ar" -> "الختم الأكبر للمراتب الـ ١٥"
                    "en" -> "Full Khatm (15 Levels)"
                    "de" -> "Vollständiger Terkip-Khatm"
                    "fr" -> "Khatm Complet (15 Niveaux)"
                    else -> "Zikir Hatmi Kemali"
                },
                desc = when (l) {
                    "ar" -> "تم بحمد الله إتمام جميع المراتب الـ ١٥ وختم الورد كاملاً."
                    "en" -> "Completed all 15 levels and sealed the full Terkip cycle."
                    "de" -> "Alle 15 Stufen wurden vollendet und der Zyklus abgeschlossen."
                    "fr" -> "Tous les 15 niveaux sont complétés avec succès."
                    else -> "15 Zikrin tamamı bitirildi ve vird hatmedildi."
                },
                icon = "💎",
                isUnlocked = completedCount >= 15,
                category = "terkip",
                requirement = when (l) {
                    "ar" -> "إتمام كافة المراتب الـ ١٥ كاملة"
                    "en" -> "Complete all 15 levels"
                    "de" -> "Alle 15 Stufen abschließen"
                    "fr" -> "Compléter l'ensemble des 15 niveaux"
                    else -> "15 zikrin tamamını ikmal etmek"
                },
                currentProgress = completedCount.toLong().coerceAtMost(15L),
                targetProgress = 15L,
                progressPercent = calcPercent(completedCount.toLong(), 15L)
            ),
            // 8. 7 Günlük Sebat
            Badge(
                id = "streak_7",
                title = when (l) {
                    "ar" -> "ثبات ٧ أيام متتالية"
                    "en" -> "7-Day Fortitude"
                    "de" -> "7-Tage-Beständigkeit"
                    "fr" -> "Régularité de 7 Jours"
                    else -> "7 Günlük Sebat"
                },
                desc = when (l) {
                    "ar" -> "المحافظة على الذكر لـ ٧ أيام متتالية دون انقطاع."
                    "en" -> "Maintained unbroken dhikr consistency for 7 consecutive days."
                    "de" -> "7 aufeinanderfolgende Tage täglicher Dhikr."
                    "fr" -> "7 jours consécutifs de récitation quotidienne sans interruption."
                    else -> "7 gün kesintisiz zikre devam edildi."
                },
                icon = "🔥",
                isUnlocked = bestStreak >= 7,
                category = "istikrar",
                requirement = when (l) {
                    "ar" -> "٧ أيام متتالية من التسبيح اليومي"
                    "en" -> "7 consecutive days of daily dhikr"
                    "de" -> "7 Tage in Folge täglicher Dhikr"
                    "fr" -> "7 jours consécutifs de dhikr"
                    else -> "7 gün kesintisiz günlük zikir"
                },
                currentProgress = bestStreak.toLong().coerceAtMost(7L),
                targetProgress = 7L,
                progressPercent = calcPercent(bestStreak.toLong(), 7L)
            ),
            // 9. 21 Günlük Alışkanlık Virdi
            Badge(
                id = "streak_21",
                title = when (l) {
                    "ar" -> "عزيمة ٢١ يوماً متتالياً"
                    "en" -> "21-Day Fortitude"
                    "de" -> "21-Tage-Entschlossenheit"
                    "fr" -> "Persévérance de 21 Jours"
                    else -> "21 Günlük Kararlılık"
                },
                desc = when (l) {
                    "ar" -> "المحافظة على الذكر لـ ٢١ يوماً وترسيخ عادة الورد."
                    "en" -> "Established a profound habit with 21 consecutive days of dhikr."
                    "de" -> "21 Tage kontinuierlicher Dhikr festigte die spirituelle Gewohnheit."
                    "fr" -> "21 jours consécutifs de régularité et d'assiduité."
                    else -> "21 gün boyunca zikir kesintisiz sürdürüldü."
                },
                icon = "✨",
                isUnlocked = bestStreak >= 21,
                category = "istikrar",
                requirement = when (l) {
                    "ar" -> "٢١ يوماً متتالياً من التسبيح والورد"
                    "en" -> "21 consecutive days of daily dhikr"
                    "de" -> "21 Tage in Folge täglicher Dhikr"
                    "fr" -> "21 jours consécutifs de dhikr"
                    else -> "21 gün kesintisiz günlük zikir"
                },
                currentProgress = bestStreak.toLong().coerceAtMost(21L),
                targetProgress = 21L,
                progressPercent = calcPercent(bestStreak.toLong(), 21L)
            ),
            // 10. 40 Günlük Çile ve Manevi Sebat
            Badge(
                id = "streak_40",
                title = when (l) {
                    "ar" -> "إخلاص الأربعين يوماً"
                    "en" -> "40-Day Spiritual Fortitude"
                    "de" -> "40-Tage-Hingabe"
                    "fr" -> "Dévouement des 40 Jours"
                    else -> "Kırk Gün İhlası"
                },
                desc = when (l) {
                    "ar" -> "إتمام أربعينية مباركة من التسبيح والمراقبة المتواصلة."
                    "en" -> "Completed a 40-day spiritual discipline with devotion."
                    "de" -> "40 Tage ununterbrochene spirituelle Beständigkeit und Einkehr."
                    "fr" -> "40 jours consécutifs de dévotion et de constance spirituelle."
                    else -> "40 gün kesintisiz manevi sebat gösterildi."
                },
                icon = "🕊️",
                isUnlocked = bestStreak >= 40,
                category = "istikrar",
                requirement = when (l) {
                    "ar" -> "٤٠ يوماً متتالياً من المحافظة على الأوراد"
                    "en" -> "40 consecutive days of fortitude"
                    "de" -> "40 Tage in Folge täglicher Dhikr"
                    "fr" -> "40 jours consécutifs de dhikr"
                    else -> "40 gün kesintisiz manevi sebat"
                },
                currentProgress = bestStreak.toLong().coerceAtMost(40L),
                targetProgress = 40L,
                progressPercent = calcPercent(bestStreak.toLong(), 40L)
            )
        )
    }
}
