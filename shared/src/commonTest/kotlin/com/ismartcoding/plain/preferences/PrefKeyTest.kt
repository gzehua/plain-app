package com.ismartcoding.plain.preferences

import kotlin.test.Test
import kotlin.test.assertEquals

class PrefKeyTest {
    @Test
    fun setting_fields_target_canonical_rust_storage_keys() {
        for ((field, key) in mapOf(
            "backgroundServiceEnabled" to "background_service_enabled",
            "desktopAccess" to "desktop_access",
            "httpPort" to "http_port",
            "screenMirrorQuality" to "screen_mirror_quality",
            "feedAutoRefreshIntervalSec" to "feed_auto_refresh_interval",
            "appHomeFeatures" to "app_home_features",
            "launcherShortcuts" to "launcher_shortcuts",
            "packageSortBy" to "pkg_sort_by",
        )) assertEquals(key, prefKey(field))
    }
}
