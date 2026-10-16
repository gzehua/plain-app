package com.ismartcoding.plain.platform.system

import com.ismartcoding.plain.platform.system.DeviceType
import com.ismartcoding.plain.platform.system.PhoneHelper
import com.ismartcoding.plain.appContext
import com.ismartcoding.plain.appContextValue
import com.ismartcoding.plain.buildType
import com.ismartcoding.plain.lib.helpers.NetworkHelper

actual fun getBuildType(): String = buildType.ifEmpty { "android" }

actual fun getPlatformName(): String = "android"

actual fun getDeviceType(): DeviceType = PhoneHelper.getDeviceType(appContextValue!!)

actual fun getDeviceIP4s(): List<String> = NetworkHelper.getDeviceIP4s().toList()

actual fun cacheDirPath(): String = appContext.cacheDir.absolutePath


actual fun getOwnPackageName(): String = appContext.packageName

actual fun dataDirPath(): String =
    appContext.filesDir.absolutePath

actual fun isDebugBuild(): Boolean = com.ismartcoding.plain.isDebugBuild()
