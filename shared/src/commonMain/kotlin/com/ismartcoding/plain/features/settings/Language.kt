package com.ismartcoding.plain.features.settings

import com.ismartcoding.plain.Prefs
import com.ismartcoding.plain.platform.system.Locale
import com.ismartcoding.plain.platform.system.setSystemLocale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

    fun applyLocale() {
        setSystemLocale(Prefs.localeValue())
    }

    fun observeLocale() = CoroutineScope(Dispatchers.Main.immediate).launch {
        Prefs.locale.collect { applyLocale() }
    }
}
