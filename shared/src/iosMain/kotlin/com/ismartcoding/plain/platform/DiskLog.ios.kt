@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.ismartcoding.plain.platform

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.NSFileManager

actual fun currentDateTimeString(): String {
    val formatter = NSDateFormatter()
    formatter.dateFormat = DiskLogFormatStrategy.LOG_DATE_FORMAT
    formatter.locale = NSLocale(localeIdentifier = "en_US_POSIX")
    return formatter.stringFromDate(NSDate())
}

actual fun clearLatestLogFile() {
    val filePath = getLatestLogFilePath()
    val fm = NSFileManager.defaultManager
    if (fm.fileExistsAtPath(filePath)) {
        fm.createFileAtPath(filePath, null, null)
    }
}
