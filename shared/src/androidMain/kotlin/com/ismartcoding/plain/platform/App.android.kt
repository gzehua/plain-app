package com.ismartcoding.plain.platform

import com.ismartcoding.plain.appContext
import com.ismartcoding.plain.features.PackageHelper
import com.ismartcoding.plain.helpers.AppHelper
import com.ismartcoding.plain.helpers.AppLogHelper
import java.io.File
import kotlin.system.exitProcess

actual fun isAppForegrounded(): Boolean = AppHelper.foregrounded()
actual fun installApk(path: String) = PackageHelper.install(appContext, File(path))
actual fun exitApp() {
    exitProcess(0)
}

internal actual fun shareLogArchive(path: String) = AppLogHelper.share(appContext, File(path))

internal actual suspend fun clearMediaCache(): Unit = com.ismartcoding.plain.lib.withIO {
    coil3.SingletonImageLoader.get(appContext).diskCache?.clear()
    coil3.SingletonImageLoader.get(appContext).memoryCache?.clear()
    VideoPlayerCacheManager.getCache()?.let { cache -> cache.keys.toList().forEach(cache::removeResource) }
}

actual suspend fun checkUpdateAsync(showToast: Boolean): Boolean? =
    AppHelper.checkUpdateAsync(appContext, showToast)

actual fun getAppIcon(packageName: String): Any? = runCatching {
    appContext.packageManager.getApplicationIcon(packageName)
}.getOrNull()

actual fun isIgnoringBatteryOptimizations(): Boolean =
    com.ismartcoding.plain.powerManager.isIgnoringBatteryOptimizations(appContext.packageName)

actual fun openBatteryOptimizationSettings() {
    val intent = android.content.Intent().apply {
        action = android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    appContext.startActivity(intent)
}
