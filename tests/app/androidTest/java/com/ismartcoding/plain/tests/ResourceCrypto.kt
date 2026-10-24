package com.ismartcoding.plain.tests

import com.ismartcoding.plain.app.TempData
import com.ismartcoding.plain.testing.CryptoFixtures
import kotlin.io.encoding.Base64

internal object ResourceCrypto {
    fun encrypt(text: String, key: ByteArray = TempData.urlToken): String =
        Base64.encode(CryptoFixtures.chaCha20Encrypt(key, text))
}
