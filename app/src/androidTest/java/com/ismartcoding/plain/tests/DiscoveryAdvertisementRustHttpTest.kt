package com.ismartcoding.plain.tests

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ismartcoding.plain.features.nearby.RustBleServiceData
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.security.MessageDigest

@RunWith(AndroidJUnit4::class)
class DiscoveryAdvertisementRustHttpTest {
    @Test
    fun rawBleScanHeaderUsesRustForDecodeAndPeerMatching() = runBlocking {
        val bytes = byteArrayOf(0xf3.toByte(), 0, 1, 2, 3, 4, 0x80.toByte(), 0xfe.toByte(), 0xff.toByte())
        val parts = RustBleServiceData.decode(bytes)!!
        assertEquals("000102030480feff", parts.shortId)
        assertTrue(parts.awareSupported)
        assertTrue(parts.awareRunning)
        assertEquals(parts, RustBleServiceData.decode(bytes + byteArrayOf(42)))
        assertNull(RustBleServiceData.decode(null))
        for (size in 0 until 9) assertNull(RustBleServiceData.decode(bytes.copyOf(size)))
        assertEquals("f16d05ec6b29248d", expectedShortId("fixture"))
    }

    private fun expectedShortId(clientId: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(clientId.toByteArray(Charsets.UTF_8))
            .take(8)
            .joinToString("") { (it.toInt() and 0xff).toString(16).padStart(2, '0') }

}
