package com.ismartcoding.plain.platform.system

interface IosBackgroundScheduler { fun schedule(atMillis: Double): String }

internal actual suspend fun scheduleBackgroundWake(atMs: Long?) {
    val error = IosPlatformRegistry.scheduleBackgroundWake(atMs?.toDouble() ?: 0.0)
    check(error.isEmpty()) { "Unable to schedule background wake: $error" }
}
