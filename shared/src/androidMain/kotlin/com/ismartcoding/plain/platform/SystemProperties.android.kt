package com.ismartcoding.plain.platform

import android.os.Build
import com.ismartcoding.plain.activityManager
import com.ismartcoding.plain.appContext
import com.ismartcoding.plain.getAppVersionCode
import com.ismartcoding.plain.getAppVersionName
import com.ismartcoding.plain.helpers.PhoneHelper
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive

internal actual fun readSystemProperties(names: List<String>): Map<String, JsonElement> {
    val display by lazy {
        val metrics = appContext.resources.displayMetrics
        Triple(metrics.widthPixels, metrics.heightPixels, metrics.density)
    }
    return names.associateWith { name ->
        when (name) {
            "name" -> JsonPrimitive(PhoneHelper.getDeviceName(appContext))
            "manufacturer" -> JsonPrimitive(Build.MANUFACTURER)
            "model" -> JsonPrimitive(Build.MODEL)
            "osVersion" -> JsonPrimitive(Build.VERSION.RELEASE)
            "appVersion" -> JsonPrimitive(getAppVersionName())
            "appBuildNumber" -> JsonPrimitive(getAppVersionCode().toString())
            "language" -> JsonPrimitive(java.util.Locale.getDefault().language)
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
