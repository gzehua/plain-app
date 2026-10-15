package com.ismartcoding.plain.features.system

import com.ismartcoding.plain.enums.WebSettingsFeature
import com.ismartcoding.plain.lib.extensions.parseEpochMillis
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.lib.sendEvent
import com.ismartcoding.plain.platform.isGranted
import kotlinx.serialization.json.*

internal object SystemAppHost {
    suspend fun handle(method: String, params: JsonObject): JsonElement = when (method) {
        "systemSetClipboard" -> {
            com.ismartcoding.plain.platform.setClipboardText("plain", params.getValue("text").jsonPrimitive.content)
            JsonHelper.jsonEncodeToElement(true)
        }
        "systemEpochMillis" -> JsonHelper.jsonEncodeToElement(params.getValue("values").jsonArray.associate { value ->
            val text = value.jsonPrimitive.content
            text to text.parseEpochMillis()
        })
        "systemPermissionFacts" -> run {
            val granted = params.getValue("permissions").jsonArray.associate { item ->
                val name = item.jsonPrimitive.content
                name to com.ismartcoding.plain.platform.Permission.valueOf(name).isGranted()
            }
            JsonHelper.jsonEncodeToElement(PermissionFacts(
                granted = granted,
            ))
        }
        "systemOpenAccessibilitySettings" -> {
            sendEvent(com.ismartcoding.plain.events.HOpenAccessibilitySettingsEvent())
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
            sendEvent(com.ismartcoding.plain.events.HOpenWebSettingsEvent(feature))
            JsonHelper.jsonEncodeToElement(true)
        }
        "systemIpv4Address" -> JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.getDeviceIP4())
        "systemProperties" -> JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.readSystemProperties(
            params.getValue("names").jsonArray.map { it.jsonPrimitive.content },
        ))
        "systemMemoryAvailable" -> JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.readMemoryAvailable())
        "systemBatterySample" -> JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.readBatterySample())
        "systemCapabilities" -> JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.getDeviceCapabilities().map { it.name })
        "systemAppFacts" -> appFacts()
        "systemLogPath" -> JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.getLatestLogFilePath())
        "systemRelaunchApp" -> {
            com.ismartcoding.plain.lib.sendEvent(com.ismartcoding.plain.events.RestartAppEvent())
            JsonHelper.jsonEncodeToElement(true)
        }
        else -> error("Unsupported provider operation")
    }

    /** The `App` contract row. Capabilities and permissions are named rather
     * than encoded: both enums are the contract's own, so there is no mapping. */
    private suspend fun appFacts(): JsonElement = JsonHelper.jsonEncodeToElement(AppFacts(
        appDir = com.ismartcoding.plain.platform.appDir(),
        deviceType = com.ismartcoding.plain.platform.getDeviceType().name,
        capabilities = com.ismartcoding.plain.platform.getDeviceCapabilities().map { it.name },
        buildChannel = com.ismartcoding.plain.enums.AppChannelType
            .fromString(com.ismartcoding.plain.buildChannel).name,
        permissions = com.ismartcoding.plain.platform.Permission.entries.filter { it.isGranted() }.map { it.name },
        downloadsDir = com.ismartcoding.plain.platform.getDownloadsDirPath(),
        debug = com.ismartcoding.plain.platform.isDebugBuild(),
    ))
}
