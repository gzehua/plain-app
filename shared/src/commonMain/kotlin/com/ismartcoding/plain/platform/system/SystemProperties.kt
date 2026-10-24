package com.ismartcoding.plain.platform.system

import com.ismartcoding.plain.platform.media.getDownloadsDirPath

import kotlinx.serialization.json.JsonElement
import com.ismartcoding.plain.buildChannel
import kotlinx.serialization.json.JsonPrimitive

internal expect fun readSystemProperties(names: List<String>): Map<String, JsonElement>

internal expect fun startupPlatformPropertyNames(): List<String>

private val startupProperties = StartupSystemProperties {
    readSystemProperties(listOf(
        "name", "deviceType", "osVersion", "appVersion", "appBuildNumber",
        "wifiAwareSupported", "files.recentCatalog", "files.recentRoot", "media.systemCatalog", "supportedAbis", "updateSource",
    ) + startupPlatformPropertyNames()) + mapOf(
        "buildChannel" to JsonPrimitive(AppChannelType.fromString(buildChannel).name),
        "cacheDir" to JsonPrimitive(cacheDirPath()),
        "downloadsDir" to JsonPrimitive(getDownloadsDirPath()),
    )
}

internal fun startupSystemProperties(): Map<String, JsonElement> = startupProperties.snapshot()

internal class StartupSystemProperties(read: () -> Map<String, JsonElement>) {
    private val properties by lazy(read)

    fun snapshot(): Map<String, JsonElement> = properties

    fun appVersion(): String {
        val version = versionName()
        val build = string("appBuildNumber")
        return if (build.isEmpty()) version else "$version ($build)"
    }

    fun deviceName(): String = string("name")

    fun versionName(): String = string("appVersion")

    fun versionCode(): Long = string("appBuildNumber").toLongOrNull() ?: 0L

    fun osVersion(): String {
        val version = string("osVersion")
        val sdk = properties["android.sdkVersion"]
        return if (sdk != null) "Android $version (API $sdk)" else "${string("osName")} $version"
    }

    private fun string(name: String): String = (properties[name] as? JsonPrimitive)?.content ?: ""
}

fun getAppVersion(): String = startupProperties.appVersion()
fun getDeviceName(): String = startupProperties.deviceName()
fun getOSVersion(): String = startupProperties.osVersion()

internal fun sampleSystemProperties(names: List<String>): Map<String, JsonElement> {
    require(names.all { it in dynamicSystemPropertyNames }) { "Only mutable system properties can be sampled" }
    return if (names.isEmpty()) emptyMap() else readSystemProperties(names.distinct())
}

private val dynamicSystemPropertyNames = setOf(
    "language", "display.width", "display.height", "display.density", "wifiAwareRunning",
)
