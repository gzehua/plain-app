@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.ismartcoding.plain.platform.system

import com.ismartcoding.plain.network.get

actual fun isAppForegrounded(): Boolean = platform.UIKit.UIApplication.sharedApplication.applicationState != platform.UIKit.UIApplicationState.UIApplicationStateBackground
actual fun installApk(path: String) {}
actual fun exitApp() {}

internal actual fun shareLogArchive(path: String) = shareFile(path)

internal actual suspend fun clearMediaCache(): Unit = com.ismartcoding.plain.lib.withIO {
    val loader = coil3.SingletonImageLoader.get(coil3.PlatformContext.INSTANCE)
    loader.diskCache?.clear()
    loader.memoryCache?.clear()
}
actual fun getAppIcon(packageName: String): Any? = null
actual fun isIgnoringBatteryOptimizations(): Boolean = false
actual fun openBatteryOptimizationSettings() {}
