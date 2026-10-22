package com.ismartcoding.plain.rust.host.provider

import com.ismartcoding.plain.app.events.HOpenAccessibilitySettingsEvent
import com.ismartcoding.plain.app.events.HOpenWebSettingsEvent
import com.ismartcoding.plain.app.events.RestartAppEvent
import com.ismartcoding.plain.features.web.WebSettingsFeature
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.lib.sendEvent
import com.ismartcoding.plain.platform.network.getNetworkType
import com.ismartcoding.plain.platform.permissions.Permission
import com.ismartcoding.plain.platform.permissions.isGranted
import com.ismartcoding.plain.platform.setClipboardText
import com.ismartcoding.plain.platform.system.clearMediaCache
import com.ismartcoding.plain.platform.system.getDeviceCapabilities
import com.ismartcoding.plain.platform.system.readBatterySample
import com.ismartcoding.plain.platform.system.sampleSystemProperties
import com.ismartcoding.plain.platform.system.scheduleBackgroundWake
import com.ismartcoding.plain.rust.host.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

internal object SystemAppHost {
    internal suspend fun handle(method: String, params: JsonObject): HostResult = when (method) {
        "systemSetClipboard" -> {
            setClipboardText("plain", JsonHelper.jsonDecodeFromElement<ClipboardTextRequest>(params).text)
            hostResult(true)
        }
        "systemPermissionFacts" -> run {
            val request = JsonHelper.jsonDecodeFromElement<PermissionRequest>(params)
            val granted = request.permissions.associate { name ->
                name to Permission.valueOf(name).isGranted()
            }
            hostResult(PermissionFacts(
                granted = granted,
            ))
        }
        "systemOpenAccessibilitySettings" -> {
            sendEvent(HOpenAccessibilitySettingsEvent())
            hostResult(true)
        }
        "systemOpenWebSettings" -> {
            val feature = JsonHelper.jsonDecodeFromElement<WebSettingsRequest>(params).feature
                ?.let { name ->
                    WebSettingsFeature.entries.firstOrNull { it.name == name }
                }
            sendEvent(HOpenWebSettingsEvent(feature))
            hostResult(true)
        }
        "systemNetworkType" -> hostResult(getNetworkType().name)
        "systemScheduleBackgroundWake" -> {
            scheduleBackgroundWake(JsonHelper.jsonDecodeFromElement<BackgroundWakeRequest>(params).atMs)
            hostResult(true)
        }
        "systemProperties" -> hostResult(sampleSystemProperties(
            JsonHelper.jsonDecodeFromElement<SystemPropertiesRequest>(params).names,
        ))
        "systemBatterySample" -> hostResult(readBatterySample())
        "systemCapabilities" -> hostResult(getDeviceCapabilities().map { it.name })
        "systemClearMediaCache" -> {
            clearMediaCache()
            hostResult(true)
        }
        "systemRelaunchApp" -> {
            sendEvent(RestartAppEvent())
            hostResult(true)
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
