package com.ismartcoding.plain.rust.host.provider

import com.ismartcoding.plain.app.events.HOpenAccessibilitySettingsEvent
import com.ismartcoding.plain.app.events.HOpenWebSettingsEvent
import com.ismartcoding.plain.app.events.RestartAppEvent
import com.ismartcoding.plain.features.web.WebSettingsFeature
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.lib.sendEvent
import com.ismartcoding.plain.platform.network.getNetworkType
import com.ismartcoding.plain.platform.network.vpnActive
import com.ismartcoding.plain.platform.permissions.Permission
import com.ismartcoding.plain.platform.permissions.isGranted
import com.ismartcoding.plain.platform.setClipboardText
import com.ismartcoding.plain.platform.system.clearMediaCache
import com.ismartcoding.plain.platform.system.getDeviceCapabilities
import com.ismartcoding.plain.platform.system.readBatterySample
import com.ismartcoding.plain.platform.system.readMemoryAvailable
import com.ismartcoding.plain.platform.system.sampleSystemProperties
import com.ismartcoding.plain.platform.system.scheduleBackgroundWake
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

internal object SystemAppHost {
    suspend fun handle(method: String, params: JsonObject): JsonElement = when (method) {
        "systemSetClipboard" -> {
            setClipboardText("plain", JsonHelper.jsonDecodeFromElement<ClipboardTextRequest>(params).text)
            JsonHelper.jsonEncodeToElement(true)
        }
        "systemPermissionFacts" -> run {
            val request = JsonHelper.jsonDecodeFromElement<PermissionRequest>(params)
            val granted = request.permissions.associate { name ->
                name to Permission.valueOf(name).isGranted()
            }
            JsonHelper.jsonEncodeToElement(PermissionFacts(
                granted = granted,
            ))
        }
        "systemOpenAccessibilitySettings" -> {
            sendEvent(HOpenAccessibilitySettingsEvent())
            JsonHelper.jsonEncodeToElement(true)
        }
        "systemOpenWebSettings" -> {
            val feature = JsonHelper.jsonDecodeFromElement<WebSettingsRequest>(params).feature
                ?.let { name ->
                    WebSettingsFeature.entries.firstOrNull { it.name == name }
                }
            sendEvent(HOpenWebSettingsEvent(feature))
            JsonHelper.jsonEncodeToElement(true)
        }
        "systemVpnActive" -> JsonHelper.jsonEncodeToElement(vpnActive())
        "systemNetworkType" -> JsonHelper.jsonEncodeToElement(getNetworkType().name)
        "systemScheduleBackgroundWake" -> {
            scheduleBackgroundWake(JsonHelper.jsonDecodeFromElement<BackgroundWakeRequest>(params).atMs)
            JsonHelper.jsonEncodeToElement(true)
        }
        "systemProperties" -> JsonHelper.jsonEncodeToElement(sampleSystemProperties(
            JsonHelper.jsonDecodeFromElement<SystemPropertiesRequest>(params).names,
        ))
        "systemMemoryAvailable" -> JsonHelper.jsonEncodeToElement(readMemoryAvailable())
        "systemBatterySample" -> JsonHelper.jsonEncodeToElement(readBatterySample())
        "systemCapabilities" -> JsonHelper.jsonEncodeToElement(getDeviceCapabilities().map { it.name })
        "systemClearMediaCache" -> {
            clearMediaCache()
            JsonHelper.jsonEncodeToElement(true)
        }
        "systemRelaunchApp" -> {
            sendEvent(RestartAppEvent())
            JsonHelper.jsonEncodeToElement(true)
        }
        else -> error("Unsupported provider operation")
    }

}

@Serializable
private data class ClipboardTextRequest(val text: String)

@Serializable
private data class PermissionRequest(val permissions: List<String>)

@Serializable
private data class WebSettingsRequest(val feature: String? = null)

@Serializable
private data class BackgroundWakeRequest(val atMs: Long? = null)

@Serializable
private data class SystemPropertiesRequest(val names: List<String>)
