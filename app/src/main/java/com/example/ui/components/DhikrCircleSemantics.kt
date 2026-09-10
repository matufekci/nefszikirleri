package com.example.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription

/**
 * Zikir Çemberi Erişilebilirlik (Accessibility / TalkBack) Semantik Tanımları
 */
fun Modifier.dhikrCircleSemantics(
    count: Long,
    target: Long,
    lang: String = "tr",
    customContentDescription: String? = null
): Modifier {
    val localizedDescription = customContentDescription ?: when (lang.lowercase()) {
        "ar" -> "زيادة عداد الذكر بمقدار واحد"
        "de" -> "Dhikr-Zähler um eins erhöhen"
        "fr" -> "Incrémenter le compteur de dhikr de un"
        "en" -> "Increment dhikr counter by one"
        else -> "Zikir sayacını bir artır"
    }
    return this.semantics(mergeDescendants = true) {
        role = Role.Button
        contentDescription = localizedDescription
        stateDescription = "$count / $target"
        liveRegion = LiveRegionMode.Polite
    }
}

