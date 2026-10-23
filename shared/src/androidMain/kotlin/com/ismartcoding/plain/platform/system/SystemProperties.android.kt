package com.ismartcoding.plain.platform.system

import com.ismartcoding.plain.platform.network.isWifiAwareSupported

import android.os.Build
import com.ismartcoding.plain.buildChannel
import com.ismartcoding.plain.activityManager
import com.ismartcoding.plain.appContext
import com.ismartcoding.plain.platform.system.PhoneHelper
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import android.os.Environment
import com.ismartcoding.plain.app.TempData
import java.util.Locale as JavaLocale
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray

internal actual fun readSystemProperties(names: List<String>): Map<String, JsonElement> {
    val packageInfo by lazy { appContext.packageManager.getPackageInfo(appContext.packageName, 0) }
    val display by lazy {
        val metrics = appContext.resources.displayMetrics
        Triple(metrics.widthPixels, metrics.heightPixels, metrics.density)
    }
    return names.associateWith { name ->
        when (name) {
            "files.recentCatalog" -> JsonPrimitive(Build.VERSION.SDK_INT >= 29)
            "media.systemCatalog" -> JsonPrimitive(true)
            "files.recentRoot" -> if (Build.VERSION.SDK_INT < 29) JsonPrimitive(Environment.getExternalStorageDirectory().absolutePath) else JsonNull
            "supportedAbis" -> JsonArray(Build.SUPPORTED_ABIS.map(::JsonPrimitive))
            "updateSource" -> if (buildChannel != AppChannelType.GITHUB.name) JsonNull else Json.parseToJsonElement("""{
                "url":"https://api.github.com/repos/plainhub/plain-app/releases/latest",
                "pageUrl":"https://github.com/plainhub/plain-app/releases/latest",
                "assets":[
                    {"requiredAbis":["arm64-v8a","x86_64","riscv64"],"nameContains":"Recommended"},
                    {"nameSuffix":"-Old-32bit.apk"}
                ]
            }""")
            "deviceType" -> JsonPrimitive(getDeviceType().name)
            "wifiAwareSupported" -> JsonPrimitive(isWifiAwareSupported)
            "wifiAwareRunning" -> JsonPrimitive(TempData.awareRunning.value)
            "name" -> JsonPrimitive(PhoneHelper.getDeviceName(appContext))
            "manufacturer" -> JsonPrimitive(Build.MANUFACTURER)
            "model" -> JsonPrimitive(Build.MODEL)
            "osVersion" -> JsonPrimitive(Build.VERSION.RELEASE)
            "appVersion" -> JsonPrimitive(packageInfo.versionName ?: "")
            "appBuildNumber" -> JsonPrimitive(packageInfo.longVersionCode.toString())
            "language" -> JsonPrimitive(JavaLocale.getDefault().language)
            "cpuArch" -> JsonPrimitive(Build.SUPPORTED_ABIS.firstOrNull() ?: "")
            "socModel" -> if (Build.VERSION.SDK_INT >= 31) JsonPrimitive(Build.SOC_MODEL) else JsonNull
            "display.width" -> JsonPrimitive(display.first)
            "display.height" -> JsonPrimitive(display.second)
            "display.density" -> JsonPrimitive(display.third)
            "android.sdkVersion" -> JsonPrimitive(Build.VERSION.SDK_INT)
            "android.versionCodeName" -> JsonPrimitive(Build.VERSION.CODENAME)
            "android.securityPatch" -> JsonPrimitive(Build.VERSION.SECURITY_PATCH)
            "android.bootloader" -> JsonPrimitive(Build.BOOTLOADER)
            "android.fingerprint" -> JsonPrimitive(Build.FINGERPRINT)
            "android.hardware" -> JsonPrimitive(Build.HARDWARE)
            "android.radioVersion" -> JsonPrimitive(Build.getRadioVersion() ?: "")
            "android.board" -> JsonPrimitive(Build.BOARD)
            "android.buildBrand" -> JsonPrimitive(Build.BRAND)
            "android.buildNumber" -> JsonPrimitive(Build.DISPLAY)
            "android.device" -> JsonPrimitive(Build.DEVICE)
            "android.javaVmVersion" -> JsonPrimitive(System.getProperty("java.vm.version") ?: "")
            "android.glEsVersion" -> JsonPrimitive(activityManager.deviceConfigurationInfo.glEsVersion)
            "android.buildTime" -> JsonPrimitive(Build.TIME)
            else -> error("Unsupported system property: $name")
        }
    }
}

internal actual fun startupPlatformPropertyNames(): List<String> = listOf(
    "manufacturer", "model", "cpuArch", "socModel",
    "android.sdkVersion", "android.versionCodeName", "android.securityPatch", "android.bootloader", "android.fingerprint",
    "android.hardware", "android.radioVersion", "android.board", "android.buildBrand", "android.buildNumber", "android.device",
    "android.javaVmVersion", "android.glEsVersion", "android.buildTime",
)
