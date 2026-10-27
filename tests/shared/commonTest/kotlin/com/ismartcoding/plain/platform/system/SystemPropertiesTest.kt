package com.ismartcoding.plain.platform.system

import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.rust.RustStartupConfig
import kotlin.test.Test
import kotlin.test.assertEquals

class SystemPropertiesTest {
    @Test
    fun startupFactsKeepTheirTypedValuesAcrossTheNativeContract() {
        val config = RustStartupConfig(debug = false, cacheDir = "/cache", name = "Device", deviceType = "PHONE", buildChannel = "release", downloadsDir = "/downloads", osName = "Android", osVersion = "15", appVersion = "3.3.25", appBuildNumber = "123", wifiAwareSupported = false, recentFilesCatalog = true, systemMediaCatalog = true, supportedAbis = listOf("arm64-v8a"), androidSdkVersion = 35)
        assertEquals(config, JsonHelper.jsonDecode<RustStartupConfig>(JsonHelper.jsonEncode(config)))
    }
}
