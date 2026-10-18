package com.ismartcoding.plain.platform.system

import com.ismartcoding.plain.rust.http.request

import com.ismartcoding.plain.platform.system.DeviceType


/**
 * Platform name for protocol headers: "android" or "ios".
 */
expect fun getPlatformName(): String

/**
 * Device type for discovery/pairing protocol.
 */
expect fun getDeviceType(): DeviceType

/**
 * All IPv4 addresses of the device. Empty list on iOS (BLE-only).
 */

fun appDir(): String = com.ismartcoding.plain.rust.request.RustContentApi.directory

/**
 * Platform cache/temp directory. Android: `Context.cacheDir`; iOS:
 * `NSCachesDirectory`. Supplied to Rust once at startup.
 */
expect fun cacheDirPath(): String

/**
 * The app's own package name / bundle identifier.
 */
expect fun getOwnPackageName(): String

expect fun dataDirPath(): String

/**
 * Whether the running app is a debuggable build.
 */
expect fun isDebugBuild(): Boolean
