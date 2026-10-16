package com.ismartcoding.plain.platform.system

import com.ismartcoding.plain.api.graphql.models.Package as AppPackage

import com.ismartcoding.plain.appContext
import com.ismartcoding.plain.features.BundleApkInstaller
import com.ismartcoding.plain.features.PackageHelper
import com.ismartcoding.plain.lib.withIO
import com.ismartcoding.plain.lib.apk.ApkParsers
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.packageManager
import kotlin.time.Instant

actual suspend fun installedPackageFacts(): List<AppPackage> = withIO { PackageHelper.installedFacts() }

actual suspend fun getPackageInfoMap(ids: List<String>): Map<String, AppPackage?> = withIO {
    PackageHelper.getPackageInfoMap(ids).mapValues { (_, packageInfo) ->
        packageInfo?.let { runCatching { PackageHelper.getPackage(it.packageName) }.getOrNull() }
    }
}

actual fun uninstallPackage(id: String) {
    PackageHelper.uninstall(appContext, id)
}

actual fun installPackage(path: String): PackageInstallResult {
    val file = java.io.File(path)
    if (!file.exists()) {
        throw IllegalArgumentException("File does not exist")
    }
    return when (file.extension.lowercase()) {
        "apk" -> installApkFile(file)
        in BundleApkInstaller.BUNDLE_EXTENSIONS -> BundleApkInstaller.install(file)
        else -> throw IllegalArgumentException("Unsupported file format. Supported: .apk, .apkm, .apks, .xapk")
    }
}

private fun installApkFile(file: java.io.File): PackageInstallResult {
    LogCat.d("Installing APK file: ${file.name}")
    val apkMeta = ApkParsers.getMetaInfo(file)
        ?: throw IllegalArgumentException("Failed to parse APK package ID")

    PackageHelper.install(appContext, file)
    val packageName = apkMeta.packageName ?: ""
    return try {
        val pkg = packageManager.getPackageInfo(packageName, 0)
        PackageInstallResult(
            packageName = packageName,
            lastUpdateTime = Instant.fromEpochMilliseconds(pkg.lastUpdateTime),
            isNew = false,
        )
    } catch (e: Exception) {
        PackageInstallResult(
            packageName = packageName,
            lastUpdateTime = null,
            isNew = true,
        )
    }
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

actual fun getManifestXml(path: String): String {
    return try {
        ApkParsers.getManifestXml(path)
    } catch (e: Exception) {
        LogCat.d(e.toString())
        ""
    }
}
