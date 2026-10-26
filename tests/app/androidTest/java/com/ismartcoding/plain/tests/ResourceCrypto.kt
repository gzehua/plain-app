package com.ismartcoding.plain.tests

import com.ismartcoding.plain.Prefs
import com.ismartcoding.plain.testing.CryptoFixtures
import kotlin.io.encoding.Base64

internal object ResourceCrypto {
    fun encrypt(text: String, key: ByteArray = Base64.decode(Prefs.urlToken.value)): String =
        Base64.encode(CryptoFixtures.chaCha20Encrypt(key, text))
}
