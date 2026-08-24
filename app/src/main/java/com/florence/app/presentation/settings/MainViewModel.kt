package com.florence.app.presentation.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florence.app.core.settings.SettingsRepository
import com.florence.app.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val settings: SettingsRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    val lang: StateFlow<String> = settings.lang.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsRepository.DEFAULT_LANG,
    )
    val theme: StateFlow<String> = settings.theme.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsRepository.DEFAULT_THEME,
    )

    init {
        // Açılışta backend'deki tercihleri çek → lokal'e yaz (GET yönü).
        viewModelScope.launch { pullPreferencesFromBackend() }
    }

    fun setLang(value: String) {
        settings.setLang(value)
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(value))
        pushPreferencesToBackend()
    }

    fun setTheme(value: String) {
        settings.setTheme(value)
        pushPreferencesToBackend()
    }

    /**
     * GET /user/preferences → {theme, lang, ...} öğelerini local Settings'e yaz.
     * Böylece web'de yapılan tema/dil seçimi uygulamaya da yansır (iki yönlü, GET yönü).
     */
    private suspend fun pullPreferencesFromBackend() {
        val prefs: JsonObject = userRepository.userPreferences().getOrNull() ?: return
        (prefs["theme"] as? JsonPrimitive)?.content?.takeIf { it.isNotBlank() }
            ?.let { settings.setTheme(it) }
        (prefs["lang"] as? JsonPrimitive)?.content?.takeIf { it.isNotBlank() }
            ?.let { settings.setLang(it) }
    }

    /**
     * Tema/dil lokal değişince backend'e fire-and-forget PUT (PUT yönü).
     * Gövde: { prefs: { "theme": ..., "lang": ... } }.
     */
    private fun pushPreferencesToBackend() {
        viewModelScope.launch {
            val prefs = buildJsonObject {
                put("theme", JsonPrimitive(settings.getThemeSync()))
                put("lang", JsonPrimitive(settings.getLangSync()))
            }
            userRepository.updateUserPreferences(prefs)
        }
    }
}