package com.example.util

import android.content.Context
import androidx.core.content.edit

/**
 * Çocuk kilidi durumu. Room şemasına dokunmamak için (migration gerektirmesin)
 * SharedPreferences'ta tutulur — kilit tamamen yerel bir oturum ayarıdır.
 *
 * Kilit açıkken sayaç dokunuşları, geri al / sıfırla ve manuel ekleme
 * ViewModel düzeyinde yok sayılır (bkz. ZikirViewModel guard'ları).
 */
object ChildLockPrefs {

    private const val PREFS = "nefs_child_lock_prefs"
    private const val KEY_ENABLED = "child_lock_enabled"

    fun isEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_ENABLED, false)

    fun setEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit { putBoolean(KEY_ENABLED, enabled) }
    }
}
