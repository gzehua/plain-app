package com.ismartcoding.plain.platform.files

import com.ismartcoding.plain.api.graphql.models.File as FileEntry

import com.ismartcoding.plain.features.files.DStorageStatsItem

/**
 * Path to the primary internal storage (e.g. /storage/emulated/0).
 * Empty string on iOS.
 */
expect fun getInternalStoragePath(): String

/**
 * Display name for internal storage.
 */
expect fun getInternalStorageName(): String

/**
 * Path to the SD card, or empty string if none.
 */
expect fun getSDCardPath(): String

/**
 * Paths to USB storage devices.
 */
expect fun getUsbDiskPaths(): List<String>

/**
 * Storage stats for internal storage.
 */
expect fun getInternalStorageStats(): DStorageStatsItem

/**
 * Storage stats for the SD card.
 */
expect fun getSDCardStorageStats(): DStorageStatsItem

/**
 * Storage stats for each USB storage device.
 */
expect fun getUSBStorageStats(): List<DStorageStatsItem>

/**
 * Canonical absolute path of [path] (resolving symlinks). Falls back to the
 * input path on platforms without a canonical notion.
 */
expect fun getCanonicalPath(path: String): String

/**
 * Returns file metadata (size, last-modified, is-directory) for [path], or null
 * if the file does not exist. Children count is 0 for non-directories.
 */
expect fun statFile(path: String): FileEntry?
