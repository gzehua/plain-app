package com.ismartcoding.plain.rust.host.provider

import com.ismartcoding.plain.features.web.WebSettingsFeature
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.lib.sendEvent
import com.ismartcoding.plain.platform.permissions.isGranted
import kotlinx.serialization.json.*

internal object SystemAppHost {
    suspend fun handle(method: String, params: JsonObject): JsonElement = when (method) {
        "systemSetClipboard" -> {
            com.ismartcoding.plain.platform.setClipboardText("plain", params.getValue("text").jsonPrimitive.content)
            JsonHelper.jsonEncodeToElement(true)
        }
        "systemPermissionFacts" -> run {
            val granted = params.getValue("permissions").jsonArray.associate { item ->
                val name = item.jsonPrimitive.content
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
            val feature = params["feature"]
                ?.takeUnless { it is JsonNull }
                ?.jsonPrimitive
                ?.content
                ?.let { name ->
                    WebSettingsFeature.entries.firstOrNull { it.name == name }
                }
            sendEvent(com.ismartcoding.plain.app.events.HOpenWebSettingsEvent(feature))
            JsonHelper.jsonEncodeToElement(true)
        }
        "systemVpnActive" -> JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.network.vpnActive())
        "systemNetworkType" -> JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.network.getNetworkType().name)
        "systemScheduleBackgroundWake" -> {
            com.ismartcoding.plain.platform.system.scheduleBackgroundWake(params["atMs"]?.takeUnless { it is JsonNull }?.jsonPrimitive?.long)
            JsonHelper.jsonEncodeToElement(true)
        }
        "systemProperties" -> JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.system.sampleSystemProperties(
            params.getValue("names").jsonArray.map { it.jsonPrimitive.content },
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
