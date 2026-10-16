package com.ismartcoding.plain.rust.host.provider

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SystemProviderHostTest {
    @Test
    fun filePrimitivesReachTheSystemProviderAlongsideSystemOperations() {
        for (method in listOf("imageDecodeBounds", "fileMetadataFacts", "systemProperties", "systemFileIndexRows")) {
            assertTrue(SystemProviderHost.accepts(method), method)
        }
    }

    @Test
    fun otherHostFamiliesKeepTheirOwnDispatchers() {
        for (method in listOf("thumbnailDecode", "fileResourceStream", "fileTaskAuthorize", "audioLibraryResolve", "imageIndexRows", "peerTransportCapabilities")) {
            assertFalse(SystemProviderHost.accepts(method), method)
        }
    }
}
