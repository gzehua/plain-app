package com.ismartcoding.plain.platform

import com.ismartcoding.plain.activityManager

internal actual fun readMemoryAvailable(): Long? {
    val sample = android.app.ActivityManager.MemoryInfo()
    activityManager.getMemoryInfo(sample)
    return sample.availMem
}
