package com.ismartcoding.plain.platform

expect fun isAppForegrounded(): Boolean

/**
 * Install an APK file at [path] via the system package installer.
 * No-op on platforms without a package installer (iOS).
 */
expect fun installApk(path: String)

/** Terminate the app process immediately. */
expect fun exitApp()

internal expect suspend fun clearMediaCache()

/**
 * Check for app updates from the release source.
 * @param showToast whether to show a toast on rate-limit / failure
 * @return true if an update is available, false if up-to-date, null on failure
 */
expect suspend fun checkUpdateAsync(showToast: Boolean): Boolean?

/**
 * Load the icon for the app with [packageName].
 * Returns a platform-specific model suitable for use with coil3 `AsyncImage`
 * (Android: `android.graphics.drawable.Drawable`; iOS: `UIImage`).
 * Returns null if the app is not installed or the icon is unavailable.
 */
expect fun getAppIcon(packageName: String): Any?

/**
 * Whether the app is currently exempt from battery optimizations.
 * Always false on iOS (no equivalent concept).
 */
expect fun isIgnoringBatteryOptimizations(): Boolean

/**
 * Open the platform's battery-optimization settings screen so the user can
 * manage exemptions. No-op on iOS.
 */
expect fun openBatteryOptimizationSettings()
