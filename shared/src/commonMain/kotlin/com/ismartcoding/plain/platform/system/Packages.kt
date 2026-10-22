package com.ismartcoding.plain.platform.system

import com.ismartcoding.plain.models.Package as AppPackage

import kotlin.time.Instant

data class PackageInstallResult(
    val packageName: String,
    val lastUpdateTime: Instant?,
    val isNew: Boolean,
)

expect suspend fun installedPackageFacts(): List<AppPackage>

/**
 * Returns a map of package id -> AppPackage (or null if not installed) for
 * each id in [ids]. Missing ids map to null.
 */
expect suspend fun getPackageInfoMap(ids: List<String>): Map<String, AppPackage?>

/**
 * Uninstall the package identified by [id]. No-op on unsupported platforms.
 */
expect fun uninstallPackage(id: String)

expect fun installPackage(paths: List<String>, packageName: String, session: Boolean): PackageInstallResult

expect fun packageInstallProfile(): PackageInstallProfile

@kotlinx.serialization.Serializable
data class PackageInstallProfile(val densityDpi: Int, val localeTags: List<String>)

/**
 * Returns the launcher icon for the package identified by [packageId], suitable
 * for use as a Coil `AsyncImage` model. Returns null on platforms without a
 * package manager (iOS) or if the icon cannot be loaded.
 *
 * Android: returns the Android `Drawable` from `PackageManager.getApplicationIcon`.
 */
expect fun getApplicationIcon(packageId: String): Any?

/**
 * Returns full detail for the package identified by [id], or null if the package
 * is not installed or cannot be queried. Always null on iOS.
 */
expect suspend fun getPackageDetail(id: String): PackageDetails?

/**
 * Whether the package identified by [id] is currently installed. Always false
 * on platforms without a package manager.
 */
expect fun isPackageInstalled(id: String): Boolean

/**
 * Whether the package identified by [id] has a launchable activity. Always
 * false on iOS.
 */
expect fun canLaunchPackage(id: String): Boolean

/**
 * Launch the package identified by [id]. No-op on iOS.
 */
expect fun launchPackage(id: String)

/**
 * Open the system "App info" settings screen for the package identified by
 * [id]. No-op on iOS.
 */
expect fun viewPackageInSettings(id: String)
