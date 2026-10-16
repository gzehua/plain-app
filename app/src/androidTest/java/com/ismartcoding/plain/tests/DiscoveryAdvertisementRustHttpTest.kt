package com.ismartcoding.plain.tests

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ismartcoding.plain.app.TempData
import com.ismartcoding.plain.features.nearby.RustDiscoveryAdvertisement
import com.ismartcoding.plain.features.nearby.RustBleServiceData
import com.ismartcoding.plain.app.*
import com.ismartcoding.plain.ui.app.*
import com.ismartcoding.plain.features.chat.*
import com.ismartcoding.plain.ui.chat.*
import com.ismartcoding.plain.features.feeds.*
import com.ismartcoding.plain.ui.feeds.*
import com.ismartcoding.plain.features.images.search.*
import com.ismartcoding.plain.features.sessions.*
import com.ismartcoding.plain.ui.sessions.*
import com.ismartcoding.plain.features.shares.*
import com.ismartcoding.plain.ui.shares.*
import com.ismartcoding.plain.platform.*
import com.ismartcoding.plain.platform.contacts.*
import com.ismartcoding.plain.platform.files.*
import com.ismartcoding.plain.ui.platform.files.*
import com.ismartcoding.plain.platform.media.*
import com.ismartcoding.plain.ui.platform.media.*
import com.ismartcoding.plain.platform.network.*
import com.ismartcoding.plain.platform.permissions.*
import com.ismartcoding.plain.app.*
import com.ismartcoding.plain.ui.app.*
import com.ismartcoding.plain.ui.navigation.*
import com.ismartcoding.plain.features.sessions.*
import com.ismartcoding.plain.ui.sessions.*
import com.ismartcoding.plain.features.settings.*
import com.ismartcoding.plain.ui.settings.*
import com.ismartcoding.plain.platform.files.*
import com.ismartcoding.plain.ui.platform.files.*
import com.ismartcoding.plain.platform.media.*
import com.ismartcoding.plain.ui.platform.media.*
import com.ismartcoding.plain.platform.notifications.*
import com.ismartcoding.plain.ui.platform.notifications.*
import com.ismartcoding.plain.platform.system.*
import com.ismartcoding.plain.ui.platform.system.*
import com.ismartcoding.plain.ui.state.*
import com.ismartcoding.plain.platform.telephony.*
import com.ismartcoding.plain.rust.http.*
import com.ismartcoding.plain.preferences.UserPrefs
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import kotlinx.coroutines.runBlocking

@RunWith(AndroidJUnit4::class)
class DiscoveryAdvertisementRustHttpTest {
    @Test
    fun currentRootPreferencesAndActualOsFactsProduceAllAdvertisingFormats() = runBlocking {
        val reply = RustDiscoveryAdvertisement.reply()
        assertEquals(TempData.clientId, reply.id)
        assertEquals(UserPrefs.deviceName.value, reply.name)
        assertEquals(UserPrefs.httpsPort.value, reply.port)
        assertEquals(getDeviceType().name, reply.deviceType)
        assertEquals(getAppVersion(), reply.version)
        assertEquals(getPlatformName(), reply.platform)
        val payload = RustDiscoveryAdvertisement.ble()
        assertEquals(9, payload.size)
        val parts = RustBleServiceData.decode(payload)!!
        assertEquals(RustBleServiceData.shortIdOf(reply.id), parts.shortId)
        assertEquals(reply.awareSupported, parts.awareSupported)
        assertEquals(reply.awareRunning, parts.awareRunning)
    }
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
        assertEquals("f16d05ec6b29248d", RustBleServiceData.shortIdOf("fixture"))
    }
}
