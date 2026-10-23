@file:OptIn(ExperimentalForeignApi::class)

package com.ismartcoding.plain.platform.system

import com.ismartcoding.plain.network.get
import coil3.PlatformContext
import coil3.SingletonImageLoader
import com.ismartcoding.plain.lib.withIO
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationState

actual fun isAppForegrounded(): Boolean = UIApplication.sharedApplication.applicationState != UIApplicationState.UIApplicationStateBackground
actual fun installApk(path: String) {}
actual fun exitApp() {}

internal actual fun shareLogArchive(path: String) = shareFile(path)

internal actual suspend fun clearMediaCache(): Unit = withIO {
    val loader = SingletonImageLoader.get(PlatformContext.INSTANCE)
    loader.diskCache?.clear()
    loader.memoryCache?.clear()
}
actual fun getAppIcon(packageName: String): Any? = null
actual fun isIgnoringBatteryOptimizations(): Boolean = false
actual fun openBatteryOptimizationSettings() {}
