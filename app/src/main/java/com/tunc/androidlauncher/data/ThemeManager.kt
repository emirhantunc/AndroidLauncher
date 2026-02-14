package com.tunc.androidlauncher.data

import android.content.Context
import android.content.SharedPreferences

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

class ThemeManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "theme_prefs",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_THEME_MODE = "theme_mode"
    }

    fun saveThemeMode(mode: ThemeMode) {
        prefs.edit().apply {
            putString(KEY_THEME_MODE, mode.name)
            apply()
        }
    }

    fun getThemeMode(): ThemeMode {
        val modeName = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name)
        return try {
            ThemeMode.valueOf(modeName ?: ThemeMode.SYSTEM.name)
        } catch (e: IllegalArgumentException) {
            ThemeMode.SYSTEM
        }
    }
}
