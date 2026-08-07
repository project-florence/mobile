package com.florence.app.core.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Uygulama ayarları: dil (default Türkçe) ve tema (web'deki 6 palet).
 * SharedPreferences tabanlı — dilin attachBaseContext'te senkron okunması gerekir.
 */
@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("florence_settings", Context.MODE_PRIVATE)

    private val _lang = MutableStateFlow(getLangSync())
    val lang: StateFlow<String> = _lang.asStateFlow()

    private val _theme = MutableStateFlow(getThemeSync())
    val theme: StateFlow<String> = _theme.asStateFlow()

    fun getLangSync(): String = prefs.getString(KEY_LANG, DEFAULT_LANG) ?: DEFAULT_LANG

    fun getThemeSync(): String = prefs.getString(KEY_THEME, DEFAULT_THEME) ?: DEFAULT_THEME

    fun setLang(value: String) {
        prefs.edit { putString(KEY_LANG, value) }
        _lang.value = value
    }

    fun setTheme(value: String) {
        prefs.edit { putString(KEY_THEME, value) }
        _theme.value = value
    }

    companion object {
        private const val KEY_LANG = "lang"
        private const val KEY_THEME = "theme"

        const val DEFAULT_LANG = "tr"
        const val DEFAULT_THEME = "florence"

        val LANGUAGES = listOf("tr" to "Türkçe", "en" to "English")
        val THEMES = listOf(
            "florence" to "Florence",
            "ocean" to "Okyanus",
            "emerald" to "Zümrüt",
            "midnight" to "Geceyarısı",
            "sunset" to "Günbatımı",
            "sepia" to "Sepya",
        )
    }
}
