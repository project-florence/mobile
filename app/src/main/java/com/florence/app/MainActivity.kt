package com.florence.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.florence.app.core.settings.LocaleHelper
import com.florence.app.core.settings.SettingsRepository
import com.florence.app.core.theme.FlorencePalettes
import com.florence.app.core.theme.FlorenceTheme
import com.florence.app.data.repository.AuthRepository
import com.florence.app.presentation.navigation.FlorenceRoot
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var settings: SettingsRepository

    @Inject
    lateinit var authRepository: AuthRepository

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
            // Doğrudan repository'den dinle: VM instance ayrımı tema güncellemesini kaçırmasın.
            val themeKey by settings.theme.collectAsStateWithLifecycle()
            val palette = FlorencePalettes.all.firstOrNull { it.key == themeKey }
                ?: FlorencePalettes.Florence
            FlorenceTheme(palette = palette) {
                FlorenceRoot()
            }
        }
        handleVerificationIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleVerificationIntent(intent)
    }

    /**
     * E-posta doğrulama deep link'ini yakala:
     * https://florencex.com.tr/api/v1/auth/verify-email?token=… → public GET'e çağır.
     */
    private fun handleVerificationIntent(intent: Intent?) {
        val token = intent?.data?.getQueryParameter("token") ?: return
        if (token.isBlank()) return
        lifecycleScope.launch {
            val ok = authRepository.verifyEmail(token).isSuccess
            val msg = if (ok) {
                authRepository.clearVerificationRequired()
                getString(R.string.verify_success)
            } else {
                getString(R.string.verify_failed)
            }
            Toast.makeText(this@MainActivity, msg, Toast.LENGTH_LONG).show()
        }
    }
}
