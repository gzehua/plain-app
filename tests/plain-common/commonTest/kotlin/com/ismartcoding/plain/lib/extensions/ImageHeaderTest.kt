package com.ismartcoding.plain.lib.extensions

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ImageHeaderTest {

    // ── getFilenameFromPath (moved from fileNameFromPath) ─────────────────

    @Test fun `getFilenameFromPath returns last path segment`() {
        assertEquals("a.txt", "/tmp/a.txt".getFilenameFromPath())
        assertEquals("a.txt", "a.txt".getFilenameFromPath())
    }

    @Test fun `getFilenameFromPath is empty for path with no separator or trailing slash`() {
        assertEquals("", "".getFilenameFromPath())
        assertEquals("", "/".getFilenameFromPath())
        assertEquals("", "/tmp/".getFilenameFromPath())
    }

    // ── isHeifHeader ───────────────────────────────────────────────────────

    private fun ftypHeader(brand: String): ByteArray =
        byteArrayOf(0, 0, 0, 24, 'f'.code.toByte(), 't'.code.toByte(), 'y'.code.toByte(), 'p'.code.toByte()) +
            brand.encodeToByteArray()

    @Test fun `isHeifHeader accepts known HEIF and AVIF brands`() {
        for (brand in listOf("heic", "heix", "hevc", "hevx", "avif")) {
            assertTrue(isHeifHeader(ftypHeader(brand)), "brand $brand should be HEIF")
        }
    }

    @Test fun `isHeifHeader rejects non-heif ftyp brands`() {
        for (brand in listOf("jpeg", "mp42", "qt  ")) {
            assertFalse(isHeifHeader(ftypHeader(brand)), "brand $brand should not be HEIF")
        }
    }

    @Test fun `isHeifHeader rejects non-ftyp boxes and short headers`() {
        assertFalse(isHeifHeader("notftyp-heic".encodeToByteArray()))
        assertFalse(isHeifHeader("heic".encodeToByteArray()))
        assertFalse(isHeifHeader(byteArrayOf()))
    }

    @Test fun `isHeifHeader requires at least 12 bytes`() {
        val header = ftypHeader("heic")
        assertTrue(isHeifHeader(header))
        assertFalse(isHeifHeader(header.copyOfRange(0, 11)))
    }
}
