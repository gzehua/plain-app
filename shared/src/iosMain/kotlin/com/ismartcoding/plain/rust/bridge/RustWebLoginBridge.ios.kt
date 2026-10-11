package com.ismartcoding.plain.rust.bridge

import com.ismartcoding.plain.rustprefs.plain_web_login_decide
import com.ismartcoding.plain.rustprefs.plain_prefs_string_free
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cstr
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString

@OptIn(ExperimentalForeignApi::class)
internal actual object RustWebLoginBridge {
    actual fun decide(requestId: String, allowed: Boolean): String = memScoped {
        val result = checkNotNull(plain_web_login_decide(requestId.cstr.ptr, allowed))
        try { result.toKString() } finally { plain_prefs_string_free(result) }
    }
}
