package com.ismartcoding.plain.preferences

internal fun prefKey(field: String): String = when (field) {
    "feedAutoRefreshIntervalSec" -> "feed_auto_refresh_interval"
    "packageSortBy" -> "pkg_sort_by"
    else -> field.replace(Regex("[A-Z]")) { "_${it.value.lowercase()}" }
}
