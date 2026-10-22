package com.ismartcoding.plain.rust.native

import com.ismartcoding.plain.rustprefs.plain_prefs_open
import com.ismartcoding.plain.rustprefs.plain_prefs_string_free
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cstr
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString

@OptIn(ExperimentalForeignApi::class)
internal actual object RustPrefsBridge {
    actual fun open(dataDir: String, cacheDir: String) = memScoped {
        checkError(plain_prefs_open(dataDir.cstr.ptr, cacheDir.cstr.ptr))
    }

    private fun checkError(pointer: kotlinx.cinterop.CPointer<kotlinx.cinterop.ByteVar>?) {
        if (pointer != null) {
            val message = pointer.toKString()
            plain_prefs_string_free(pointer)
            error(message)
        }
    }

}
