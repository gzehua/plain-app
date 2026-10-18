package com.ismartcoding.plain.ui.media

import com.ismartcoding.plain.platform.system.PlatformLock
import com.ismartcoding.plain.rust.bridge.RustCoreBridge

private val icons = FileIconCache(96, RustCoreBridge::fileIcon)

fun fileIconData(extension: String): ByteArray = icons.get(extension)

internal class FileIconCache(private val capacity: Int, private val read: (String) -> String) {
    private val lock = PlatformLock()
    private val entries = linkedMapOf<String, ByteArray>()
    init { require(capacity > 0) }

    fun get(extension: String): ByteArray = lock.withLock {
        val key = extension.trim().trimStart('.').lowercase()
        entries[key]?.let { return@withLock it }
        val bytes = read(key).encodeToByteArray()
        check(bytes.isNotEmpty()) { "Embedded file icon missing" }
        if (entries.size >= capacity) entries.remove(entries.keys.first())
        entries[key] = bytes
        bytes
    }
}
