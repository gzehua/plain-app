package com.ismartcoding.plain.rust.bridge

import com.ismartcoding.plain.rustprefs.plain_discovery_action
import com.ismartcoding.plain.rustprefs.plain_prefs_string_free
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cstr
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString

@OptIn(ExperimentalForeignApi::class)
internal actual object RustDiscoveryBridge {
    actual fun action(factsJson: String, requestJson: String): String = memScoped {
        val result = checkNotNull(plain_discovery_action(factsJson.cstr.ptr, requestJson.cstr.ptr))
        try { result.toKString() } finally { plain_prefs_string_free(result) }
    }
}