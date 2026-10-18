package com.ismartcoding.plain.preferences

import kotlin.test.Test
import kotlin.test.assertEquals

class UserPreferenceKeyTest {
    @Test
    fun setting_projection_fields_target_the_existing_rust_storage_keys() {
        for ((field, key) in mapOf(
            "service" to "service",
            "desktopAccess" to "desktop_access",
            "httpPort" to "http_port",
            "screenMirrorQuality" to "screen_mirror_quality",
            "feedAutoRefreshIntervalSec" to "feed_auto_refresh_interval",
            "homeFeatures" to "home_features_v2",
            "launcherShortcuts" to "launcher_shortcuts_v1",
            "packageSortBy" to "pkg_sort_by",
        )) assertEquals(key, userPreferenceKey(field))
    }
}
