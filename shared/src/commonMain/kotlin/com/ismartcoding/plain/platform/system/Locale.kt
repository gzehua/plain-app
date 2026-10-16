package com.ismartcoding.plain.platform.system



data class Locale(
    val language: String,
    val country: String,
) {
    val isZhCN: Boolean get() = language == "zh" && country == "CN"

    /**
     * Human-readable display name with elegant handling for Chinese variants.
     */
    fun getElegantDisplayName(): String = when {
        language == "zh" && country == "TW" -> "繁體中文"
        language == "zh" && country == "CN" -> "简体中文"
        else -> getLocaleDisplayName(this)
    }
}

expect fun currentLocale(): Locale

/**
 * Platform-specific display name for a locale (e.g. "English (United States)").
 */
expect fun getLocaleDisplayName(locale: Locale): String

/**
 * Apply the given locale to the system (Android resources + JVM default).
 * On iOS this is a no-op (handled via system locale).
 */
expect fun setSystemLocale(locale: Locale?)

