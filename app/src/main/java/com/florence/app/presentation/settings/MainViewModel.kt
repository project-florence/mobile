package com.florence.app.presentation.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florence.app.core.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val settings: SettingsRepository,
) : ViewModel() {

    val lang: StateFlow<String> = settings.lang.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsRepository.DEFAULT_LANG,
    )
    val theme: StateFlow<String> = settings.theme.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsRepository.DEFAULT_THEME,
    )

    fun setLang(value: String) {
        settings.setLang(value)
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(value))
    }

    fun setTheme(value: String) {
        settings.setTheme(value)
    }
}
