package com.ismartcoding.plain.platform.system

import com.ismartcoding.plain.platform.system.DeviceType
import com.ismartcoding.plain.platform.system.PhoneHelper
import com.ismartcoding.plain.appContext
import com.ismartcoding.plain.appContextValue
import com.ismartcoding.plain.isDebugBuild as plainIsDebugBuild



actual fun getDeviceType(): DeviceType = PhoneHelper.getDeviceType(appContextValue!!)


actual fun cacheDirPath(): String = appContext.cacheDir.absolutePath


actual fun getOwnPackageName(): String = appContext.packageName

actual fun dataDirPath(): String =
    appContext.filesDir.absolutePath

actual fun isDebugBuild(): Boolean = plainIsDebugBuild()
