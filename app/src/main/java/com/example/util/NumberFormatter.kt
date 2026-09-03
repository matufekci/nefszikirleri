package com.example.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object NumberFormatter {

    fun format(num: Long, lang: String = "tr"): String {
        return try {
            val locale = when (lang.lowercase()) {
                "ar" -> Locale.forLanguageTag("ar-SA")
                "de" -> Locale.GERMANY
                "fr" -> Locale.FRANCE
                "en" -> Locale.US
                else -> Locale.forLanguageTag("tr-TR")
            }
            NumberFormat.getInstance(locale).format(num)
        } catch (e: Exception) {
            num.toString()
        }
    }

    fun format(num: Int, lang: String = "tr"): String = format(num.toLong(), lang)

    fun formatNumber(num: Long, lang: String = "tr"): String = format(num, lang)

    fun formatNumber(num: Int, lang: String = "tr"): String = format(num.toLong(), lang)

    fun formatDate(timestamp: Long?, lang: String = "tr", notStartedText: String = "Henüz başlanmadı"): String {
        if (timestamp == null || timestamp <= 0) return notStartedText
        return try {
            val locale = when (lang.lowercase()) {
                "ar" -> Locale.forLanguageTag("ar-SA")
                "de" -> Locale.GERMANY
                "fr" -> Locale.FRANCE
                "en" -> Locale.US
                else -> Locale.forLanguageTag("tr-TR")
            }
            val sdf = SimpleDateFormat("dd.MM.yyyy", locale)
            sdf.format(Date(timestamp))
        } catch (e: Exception) {
            notStartedText
        }
    }

    fun getDateKey(date: Date = Date()): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(date)
    }

    fun getDayLabel(date: Date, lang: String): String {
        val locale = when (lang.lowercase()) {
            "ar" -> Locale.forLanguageTag("ar-SA")
            "de" -> Locale.GERMAN
            "fr" -> Locale.FRENCH
            "en" -> Locale.ENGLISH
            else -> Locale.forLanguageTag("tr-TR")
        }
        val sdf = SimpleDateFormat("EEE", locale)
        return sdf.format(date)
    }

    fun getMonthLabel(date: Date, lang: String): String {
        val locale = when (lang.lowercase()) {
            "ar" -> Locale.forLanguageTag("ar-SA")
            "de" -> Locale.GERMAN
            "fr" -> Locale.FRENCH
            "en" -> Locale.ENGLISH
            else -> Locale.forLanguageTag("tr-TR")
        }
        val sdf = SimpleDateFormat("MMM", locale)
        return sdf.format(date)
    }
}
