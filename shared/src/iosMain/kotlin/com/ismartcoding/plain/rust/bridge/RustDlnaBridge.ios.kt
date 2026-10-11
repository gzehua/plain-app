package com.ismartcoding.plain.rust.bridge

import com.ismartcoding.plain.rustprefs.plain_dlna_receiver
import com.ismartcoding.plain.rustprefs.plain_prefs_string_free
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cstr
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString

@OptIn(ExperimentalForeignApi::class)
internal actual object RustDlnaBridge {
    actual fun receiver(commandJson: String): String = memScoped {
        val result = checkNotNull(plain_dlna_receiver(commandJson.cstr.ptr))
        try { result.toKString() } finally { plain_prefs_string_free(result) }
    }
}
