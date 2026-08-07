package com.florence.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.florence.app.core.settings.LocaleHelper
import com.florence.app.core.settings.SettingsRepository
import com.florence.app.core.theme.FlorencePalettes
import com.florence.app.core.theme.FlorenceTheme
import com.florence.app.presentation.navigation.FlorenceRoot
import com.florence.app.presentation.settings.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var settings: SettingsRepository

    override fun attachBaseContext(newBase: android.content.Context) {
        // Dil seçimini kaynaklara uygula (default Türkçe).
        // Not: attachBaseContext, Hilt injection'dan önce çalışır — prefs'i doğrudan oku.
        val prefs = newBase.getSharedPreferences("florence_settings", android.content.Context.MODE_PRIVATE)
        val lang = prefs.getString("lang", SettingsRepository.DEFAULT_LANG) ?: SettingsRepository.DEFAULT_LANG
        super.attachBaseContext(LocaleHelper.apply(newBase, lang))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val mainViewModel: MainViewModel = hiltViewModel()
            val lang by mainViewModel.lang.collectAsStateWithLifecycle()
            val themeKey by mainViewModel.theme.collectAsStateWithLifecycle()
            val palette = FlorencePalettes.all.firstOrNull { it.name.lowercase() == themeKey }
                ?: FlorencePalettes.Florence
            FlorenceTheme(palette = palette) {
                FlorenceRoot()
            }
        }
    }
}
