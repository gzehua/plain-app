package com.ismartcoding.plain.features.settings

import com.ismartcoding.plain.preferences.*
import com.ismartcoding.plain.platform.system.Locale
import com.ismartcoding.plain.platform.system.setSystemLocale

object Language {
    val locales =
        listOf(
            Locale("en", "US"),
            Locale("zh", "CN"),
            Locale("zh", "TW"),
            Locale("es", ""),
            Locale("ja", ""),
            Locale("nl", ""),
            Locale("it", ""),
            Locale("hi", ""),
            Locale("fr", ""),
            Locale("ru", ""),
            Locale("bn", ""),
            Locale("de", ""),
            Locale("pt", ""),
            Locale("ta", ""),
            Locale("ko", ""),
            Locale("tr", ""),
            Locale("vi", ""),
        )

    suspend fun initLocaleAsync() {
        val locale = Prefs.localeValue()
        if (locale != null) {
            setSystemLocale(locale)
        }
    }
}
