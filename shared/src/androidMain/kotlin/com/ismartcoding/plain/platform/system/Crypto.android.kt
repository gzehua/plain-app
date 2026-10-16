package com.ismartcoding.plain.platform.system

import java.security.SecureRandom
import kotlin.io.encoding.Base64

actual fun generateChaCha20Key(): String {
    val bytes = ByteArray(32)
    SecureRandom.getInstanceStrong().nextBytes(bytes)
    return Base64.encode(bytes)
}
