package com.ismartcoding.plain.platform.system

actual fun newPlatformLock(): Any = Any()

actual fun <T> runPlatformLocked(lock: Any, block: () -> T): T = synchronized(lock, block)
