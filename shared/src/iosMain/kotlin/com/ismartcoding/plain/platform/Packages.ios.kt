package com.ismartcoding.plain.platform

import com.ismartcoding.plain.graphql.models.Package as AppPackage

import com.ismartcoding.plain.features.file.FileSortBy

actual suspend fun installedPackageFacts(): List<AppPackage> = emptyList()

actual suspend fun getPackageInfoMap(ids: List<String>): Map<String, AppPackage?> =
    ids.associateWith { null }

actual fun uninstallPackage(id: String) = Unit

actual fun installPackage(path: String): PackageInstallResult =
    throw IllegalArgumentException("APK installation is not supported on iOS")

actual fun getApplicationIcon(packageId: String): Any? = null

actual suspend fun getPackageDetail(id: String): PackageDetails? = null

actual fun isPackageInstalled(id: String): Boolean = false

actual fun canLaunchPackage(id: String): Boolean = false

actual fun launchPackage(id: String) = Unit

actual fun viewPackageInSettings(id: String) = Unit

actual fun getManifestXml(path: String): String = ""
