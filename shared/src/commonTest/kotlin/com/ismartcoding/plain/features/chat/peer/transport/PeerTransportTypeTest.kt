package com.ismartcoding.plain.features.chat.peer.transport

import com.ismartcoding.plain.lib.JsonHelper
import kotlin.test.Test
import kotlin.test.assertEquals

class PeerTransportTypeTest {
    @Test
    fun sdkCapabilitiesKeepTheirCanonicalWireNames() {
        val capabilities = listOf(PeerTransportType.AWARE, PeerTransportType.BLE)
        assertEquals("[\"AWARE\",\"BLE\"]", JsonHelper.jsonEncodeToElement(capabilities).toString())
        assertEquals(capabilities, JsonHelper.jsonDecode<List<PeerTransportType>>("[\"AWARE\",\"BLE\"]"))
    }

    @Test
    fun noOptionalSdkTransportsStillSerializesWithoutReflectiveFallback() {
        assertEquals("[]", JsonHelper.jsonEncodeToElement(emptyList<PeerTransportType>()).toString())
    }
}
