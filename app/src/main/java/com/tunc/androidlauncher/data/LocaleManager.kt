package com.tunc.androidlauncher.data

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

class LocaleManager(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "locale_prefs"
        private const val KEY_LANGUAGE = "selected_language"

        const val LANGUAGE_SYSTEM = "system"
        const val LANGUAGE_ENGLISH = "en"
        const val LANGUAGE_TURKISH = "tr"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun setLanguage(languageCode: String) {
        prefs.edit().putString(KEY_LANGUAGE, languageCode).apply()

        if (languageCode == LANGUAGE_SYSTEM) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
        } else {
            val locale = LocaleListCompat.forLanguageTags(languageCode)
            AppCompatDelegate.setApplicationLocales(locale)
        }
    }

    fun getCurrentLanguage(): String {
        return prefs.getString(KEY_LANGUAGE, LANGUAGE_SYSTEM) ?: LANGUAGE_SYSTEM
    }

    fun getLanguageDisplayName(languageCode: String): String {
        return when (languageCode) {
            LANGUAGE_SYSTEM -> {
                val systemLocale = Locale.getDefault()
                when (systemLocale.language) {
                    "tr" -> "Sistem Dili (Türkçe)"
                    else -> "System Language (English)"
                }
            }
            LANGUAGE_ENGLISH -> "English"
            LANGUAGE_TURKISH -> "Türkçe"
            else -> languageCode
        }
    }

    fun getAvailableLanguages(): List<LanguageOption> {
        return listOf(
            LanguageOption(LANGUAGE_SYSTEM, getLanguageDisplayName(LANGUAGE_SYSTEM)),
            LanguageOption(LANGUAGE_ENGLISH, "English"),
            LanguageOption(LANGUAGE_TURKISH, "Türkçe")
        )
    }
}

data class LanguageOption(
    val code: String,
    val displayName: String
)
