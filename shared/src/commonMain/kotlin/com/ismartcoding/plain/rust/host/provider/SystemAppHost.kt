package com.ismartcoding.plain.rust.host.provider

import kotlinx.serialization.Serializable

import com.ismartcoding.plain.features.web.WebSettingsFeature
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.lib.sendEvent
import com.ismartcoding.plain.platform.permissions.isGranted
import kotlinx.serialization.json.*

internal object SystemAppHost {
    suspend fun handle(method: String, params: JsonObject): JsonElement = when (method) {
        "systemSetClipboard" -> {
            com.ismartcoding.plain.platform.setClipboardText("plain", JsonHelper.jsonDecodeFromElement<ClipboardTextRequest>(params).text)
            JsonHelper.jsonEncodeToElement(true)
        }
        "systemPermissionFacts" -> run {
            val request = JsonHelper.jsonDecodeFromElement<PermissionRequest>(params)
            val granted = request.permissions.associate { name ->
                name to com.ismartcoding.plain.platform.permissions.Permission.valueOf(name).isGranted()
            }
            JsonHelper.jsonEncodeToElement(PermissionFacts(
                granted = granted,
            ))
        }
        "systemOpenAccessibilitySettings" -> {
            sendEvent(com.ismartcoding.plain.app.events.HOpenAccessibilitySettingsEvent())
            JsonHelper.jsonEncodeToElement(true)
        }
        "systemOpenWebSettings" -> {
            val feature = JsonHelper.jsonDecodeFromElement<WebSettingsRequest>(params).feature
                ?.let { name ->
                    WebSettingsFeature.entries.firstOrNull { it.name == name }
                }
            sendEvent(com.ismartcoding.plain.app.events.HOpenWebSettingsEvent(feature))
            JsonHelper.jsonEncodeToElement(true)
        }
        "systemVpnActive" -> JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.network.vpnActive())
        "systemNetworkType" -> JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.network.getNetworkType().name)
        "systemScheduleBackgroundWake" -> {
            com.ismartcoding.plain.platform.system.scheduleBackgroundWake(JsonHelper.jsonDecodeFromElement<BackgroundWakeRequest>(params).atMs)
            JsonHelper.jsonEncodeToElement(true)
        }
        "systemProperties" -> JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.system.sampleSystemProperties(
            JsonHelper.jsonDecodeFromElement<SystemPropertiesRequest>(params).names,
        ))
        "systemMemoryAvailable" -> JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.system.readMemoryAvailable())
        "systemBatterySample" -> JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.system.readBatterySample())
        "systemCapabilities" -> JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.system.getDeviceCapabilities().map { it.name })
        "systemClearMediaCache" -> {
            com.ismartcoding.plain.platform.system.clearMediaCache()
            JsonHelper.jsonEncodeToElement(true)
        }
        "systemRelaunchApp" -> {
            com.ismartcoding.plain.lib.sendEvent(com.ismartcoding.plain.app.events.RestartAppEvent())
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
