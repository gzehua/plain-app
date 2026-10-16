package com.ismartcoding.plain.platform.system

import com.ismartcoding.plain.lib.crypto.fillSecureRandom
import kotlin.io.encoding.Base64

fun generateChaCha20Key(): String {
    val bytes = ByteArray(32)
    check(fillSecureRandom(bytes)) { "Secure random generation failed" }
    return Base64.encode(bytes)
}
