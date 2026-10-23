@file:OptIn(ExperimentalForeignApi::class)

package com.ismartcoding.plain.platform.media

import kotlinx.cinterop.CValue
import platform.CoreMedia.CMTime
import platform.CoreMedia.CMTimeGetSeconds
import kotlinx.cinterop.ExperimentalForeignApi

internal fun mediaTimeMs(time: CValue<CMTime>): Long {
    val seconds = CMTimeGetSeconds(time)
    return if (seconds.isFinite() && seconds >= 0.0) (seconds * 1000.0).toLong() else 0L
}
