package com.ismartcoding.plain.rust.bridge

import com.ismartcoding.plain.rustprefs.plain_prefs_open
import com.ismartcoding.plain.rustprefs.plain_prefs_string_free
import com.ismartcoding.plain.rustprefs.plain_prefs_user_snapshot
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cstr
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString

@OptIn(ExperimentalForeignApi::class)
internal actual object RustPrefsBridge {
    actual fun open(dataDir: String, cacheDir: String) = memScoped {
        checkError(plain_prefs_open(dataDir.cstr.ptr, cacheDir.cstr.ptr))
    }

    actual fun userSnapshot(): String = memScoped {
        val result = checkNotNull(plain_prefs_user_snapshot())
        try {
            result.toKString().also { if (it.startsWith(ERROR_PREFIX)) error(it.removePrefix(ERROR_PREFIX)) }
        } finally {
            plain_prefs_string_free(result)
        }
    }

    private fun checkError(pointer: kotlinx.cinterop.CPointer<kotlinx.cinterop.ByteVar>?) {
        if (pointer != null) {
            val message = pointer.toKString()
            plain_prefs_string_free(pointer)
            error(message)
        }
    }

    /** `plain_prefs_user_snapshot` has no error channel, so failures come back tagged. */
    private const val ERROR_PREFIX = "ERROR:"
}