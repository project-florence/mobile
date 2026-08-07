package com.florence.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.florence.app.core.settings.LocaleHelper
import com.florence.app.core.settings.SettingsRepository
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class FlorenceApp : Application() {

    @Inject
    lateinit var settings: SettingsRepository

    override fun onCreate() {
        super.onCreate()
        // Default dil Türkçe; ayarlardan seçim yapılmadıysa tr uygulanır.
        LocaleHelper.apply(this, settings.getLangSync())
        // AppCompat locales'i de senkron tut (sistem dil menüsüyle uyum için).
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(settings.getLangSync()))
    }
}
