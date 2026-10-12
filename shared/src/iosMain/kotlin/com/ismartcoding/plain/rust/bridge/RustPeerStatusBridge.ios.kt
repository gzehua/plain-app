package com.ismartcoding.plain.rust.bridge

import com.ismartcoding.plain.rustprefs.plain_peer_status_action
import com.ismartcoding.plain.rustprefs.plain_prefs_string_free
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cstr
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString

@OptIn(ExperimentalForeignApi::class)
internal actual object RustPeerStatusBridge {
    actual fun action(commandJson: String): String = memScoped {
        val result = checkNotNull(plain_peer_status_action(commandJson.cstr.ptr))
        try { result.toKString() } finally { plain_prefs_string_free(result) }
    }
}