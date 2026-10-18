package com.ismartcoding.plain.preferences

internal fun userPreferenceKey(field: String): String = when (field) {
    "feedAutoRefreshIntervalSec" -> "feed_auto_refresh_interval"
    "homeFeatures" -> "home_features_v2"
    "launcherShortcuts" -> "launcher_shortcuts_v1"
    "packageSortBy" -> "pkg_sort_by"
    else -> field.replace(Regex("[A-Z]")) { "_${it.value.lowercase()}" }
}
