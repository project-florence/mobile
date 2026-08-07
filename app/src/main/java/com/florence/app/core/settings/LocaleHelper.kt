package com.florence.app.core.settings

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/**
 * Uygulama düzeyinde dil zorlaması (AppCompat locale yönteminden bağımsız).
 * attachBaseContext'te çağrılır; Context'in kaynakları seçilen dile çevrilir.
 */
object LocaleHelper {

    fun apply(context: Context, lang: String): Context {
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}
