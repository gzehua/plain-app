package com.ismartcoding.plain.platform.system

import com.ismartcoding.plain.rust.RustStartupConfig
import kotlinx.serialization.json.JsonElement

internal expect fun readSystemProperties(names: List<String>): Map<String, JsonElement>

internal expect fun createRustStartupConfig(): RustStartupConfig

internal val rustStartupConfig: RustStartupConfig by lazy(::createRustStartupConfig)

fun getAppVersion(): String {
    val config = rustStartupConfig
    return if (config.appBuildNumber.isEmpty()) config.appVersion else "${config.appVersion} (${config.appBuildNumber})"
}

fun getDeviceName(): String = rustStartupConfig.name

fun getOSVersion(): String {
    val config = rustStartupConfig
    return if (config.androidSdkVersion != null) {
        "Android ${config.osVersion} (API ${config.androidSdkVersion})"
    } else {
        "${config.osName} ${config.osVersion}"
    }
}
