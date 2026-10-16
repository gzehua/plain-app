package com.ismartcoding.plain.platform.system

import com.ismartcoding.plain.lib.crypto.fillSecureRandom
import kotlin.io.encoding.Base64

actual fun generateChaCha20Key(): String {
    val bytes = ByteArray(32)
    if (!fillSecureRandom(bytes)) {
        throw RuntimeException("SecRandomCopyBytes failed")
    }
    return Base64.encode(bytes)
}
