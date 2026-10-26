package com.ismartcoding.plain.platform.system

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized

class PlatformLock {
    private val lock = SynchronizedObject()

    fun <T> withLock(block: () -> T): T = synchronized(lock, block)
}
