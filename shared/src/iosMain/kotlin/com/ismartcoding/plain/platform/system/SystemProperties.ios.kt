package com.ismartcoding.plain.platform.system

import com.ismartcoding.plain.platform.network.isWifiAwareSupported

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import platform.CoreGraphics.CGRectGetHeight
import platform.CoreGraphics.CGRectGetWidth
import platform.Foundation.NSBundle
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.languageCode
import platform.UIKit.UIScreen
import platform.UIKit.UIDevice

@OptIn(ExperimentalForeignApi::class)
internal actual fun readSystemProperties(names: List<String>): Map<String, JsonElement> {
    val display by lazy {
        val screen = UIScreen.mainScreen
        val bounds = screen.bounds
        val scale = screen.scale
        Triple((CGRectGetWidth(bounds) * scale).toInt(), (CGRectGetHeight(bounds) * scale).toInt(), scale)
    }
    return names.associateWith { name ->
        when (name) {
            "files.recentCatalog" -> JsonPrimitive(false)
            "files.recentRoot" -> kotlinx.serialization.json.JsonNull
            "supportedAbis" -> kotlinx.serialization.json.JsonArray(emptyList())
            "updateSource" -> kotlinx.serialization.json.JsonNull
            "deviceType" -> JsonPrimitive(getDeviceType().name)
            "wifiAwareSupported" -> JsonPrimitive(isWifiAwareSupported)
            "wifiAwareRunning" -> JsonPrimitive(com.ismartcoding.plain.app.TempData.awareRunning.value)
            "name" -> JsonPrimitive(UIDevice.currentDevice.name)
            "osName" -> JsonPrimitive(UIDevice.currentDevice.systemName)
            "osVersion" -> JsonPrimitive(UIDevice.currentDevice.systemVersion)
            "appVersion" -> JsonPrimitive(NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: "")
            "appBuildNumber" -> JsonPrimitive(NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleVersion") as? String ?: "")
            "language" -> JsonPrimitive(NSLocale.currentLocale.languageCode ?: "")
            "display.width" -> JsonPrimitive(display.first)
            "display.height" -> JsonPrimitive(display.second)
            "display.density" -> JsonPrimitive(display.third)
            else -> error("Unsupported system property: $name")
        }
    }
}

internal actual fun startupPlatformPropertyNames(): List<String> = listOf("osName")
