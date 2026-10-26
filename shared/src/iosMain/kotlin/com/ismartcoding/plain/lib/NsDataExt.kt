@file:OptIn(ExperimentalForeignApi::class)

package com.ismartcoding.plain.lib

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.readBytes
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.dataWithBytes

fun ByteArray.toNSData(): NSData {
    return if (isEmpty()) {
        NSData()
    } else {
        usePinned { pinned ->
            NSData.dataWithBytes(pinned.addressOf(0), size.toULong())
        }
    }
}

fun NSData.toByteArray(): ByteArray {
    val size = length
    check(size <= Int.MAX_VALUE.toULong()) { "NSData exceeds ByteArray size limit" }
    if (size == 0UL) return ByteArray(0)
    return checkNotNull(bytes) { "NSData has no bytes" }.readBytes(size.toInt())
}
