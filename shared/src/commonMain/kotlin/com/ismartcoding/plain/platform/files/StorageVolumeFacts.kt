package com.ismartcoding.plain.platform.files

import com.ismartcoding.plain.platform.system.appDir

import com.ismartcoding.plain.features.files.DriveType
import com.ismartcoding.plain.rust.host.files.MountFacts

internal fun storageVolumeFacts(): List<MountFacts> = buildList {
    val internal = getInternalStorageStats()
    add(MountFacts(getInternalStorageName(), getInternalStoragePath(), internal.totalBytes, internal.freeBytes, DriveType.INTERNAL_STORAGE))
    val sdPath = getSDCardPath()
    if (sdPath.isNotEmpty()) {
        val stats = getSDCardStorageStats()
        add(MountFacts("", sdPath, stats.totalBytes, stats.freeBytes, DriveType.SDCARD))
    }
    val usbStats = getUSBStorageStats()
    getUsbDiskPaths().forEachIndexed { index, path ->
        usbStats.getOrNull(index)?.let { stats -> add(MountFacts("", path, stats.totalBytes, stats.freeBytes, DriveType.USB_STORAGE)) }
    }
    if (appDir().isNotEmpty()) add(MountFacts("", appDir(), 0, 0, DriveType.APP))
}
