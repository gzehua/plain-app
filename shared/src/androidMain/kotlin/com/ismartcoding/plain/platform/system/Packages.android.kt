package com.ismartcoding.plain.platform.system

import com.ismartcoding.plain.models.Package as AppPackage

import com.ismartcoding.plain.appContext
import com.ismartcoding.plain.features.PackageHelper
import com.ismartcoding.plain.lib.withIO
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.packageManager
import kotlin.time.Instant
import java.io.File

actual suspend fun installedPackageFacts(): List<AppPackage> = withIO { PackageHelper.installedFacts() }

actual suspend fun getPackageInfoMap(ids: List<String>): Map<String, AppPackage?> = withIO {
    PackageHelper.getPackageInfoMap(ids).mapValues { (_, packageInfo) ->
        packageInfo?.let { runCatching { PackageHelper.getPackage(it.packageName) }.getOrNull() }
    }
}

actual fun uninstallPackage(id: String) {
    PackageHelper.uninstall(appContext, id)
}

actual fun packageInstallProfile(): PackageInstallProfile {
    val configuration = appContext.resources.configuration
    return PackageInstallProfile(configuration.densityDpi, configuration.locales.toLanguageTags().split(','))
}

actual fun installPackage(paths: List<String>, packageName: String, session: Boolean): PackageInstallResult {
    require(paths.isNotEmpty())
    val files = paths.map { File(it) }
    require(files.all { it.isFile }) { "APK file not found" }
    val installed = runCatching { packageManager.getPackageInfo(packageName, 0) }.getOrNull()
    if (session) PackageInstallSession.write(files) else {
        require(files.size == 1)
        PackageHelper.install(appContext, files.single())
    }
    return PackageInstallResult(packageName, installed?.let { Instant.fromEpochMilliseconds(it.lastUpdateTime) }, installed == null)
}

actual fun getApplicationIcon(packageId: String): Any? {
    return try {
        val appInfo = packageManager.getApplicationInfo(packageId, 0)
        packageManager.getApplicationIcon(appInfo)
    } catch (e: Exception) {
        LogCat.d(e.toString())
        null
    }
}

actual suspend fun getPackageDetail(id: String): PackageDetails? = withIO {
    runCatching { PackageHelper.getPackageDetail(id) }.getOrNull()
}

actual fun isPackageInstalled(id: String): Boolean = PackageHelper.isInstalled(id)

actual fun canLaunchPackage(id: String): Boolean = PackageHelper.canLaunch(id)

actual fun launchPackage(id: String) {
    PackageHelper.launch(appContext, id)
}

actual fun viewPackageInSettings(id: String) {
    PackageHelper.viewInSettings(appContext, id)
}
