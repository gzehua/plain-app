package com.ismartcoding.plain.platform.system

import com.ismartcoding.plain.models.Package as AppPackage

import com.ismartcoding.plain.features.files.FileSortBy

actual suspend fun installedPackageFacts(): List<AppPackage> = emptyList()

actual suspend fun getPackageInfoMap(ids: List<String>): Map<String, AppPackage?> =
    ids.associateWith { null }

actual fun uninstallPackage(id: String) = Unit

actual fun installPackage(paths: List<String>, packageName: String, session: Boolean): PackageInstallResult =
    error("Package installation is unavailable")

actual fun packageInstallProfile(): PackageInstallProfile = error("Package installation profile is unavailable")

actual fun getApplicationIcon(packageId: String): Any? = null

actual suspend fun getPackageDetail(id: String): PackageDetails? = null

actual fun isPackageInstalled(id: String): Boolean = false

actual fun canLaunchPackage(id: String): Boolean = false

actual fun launchPackage(id: String) = Unit

actual fun viewPackageInSettings(id: String) = Unit
