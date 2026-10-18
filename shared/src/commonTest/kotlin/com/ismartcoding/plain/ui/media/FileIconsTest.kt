package com.ismartcoding.plain.ui.media

import kotlin.test.*

class FileIconsTest {
    @Test
    fun repeatedFileRowsReuseOneNativeReadAndTheSameByteArray() {
        val reads = mutableListOf<String>()
        val cache = FileIconCache(2) { reads.add(it); "<svg>$it</svg>" }
        val first = cache.get(" .PDF ")
        repeat(100) { assertSame(first, cache.get("pdf")) }
        assertEquals(listOf("pdf"), reads)
        assertEquals("<svg>pdf</svg>", first.decodeToString())
    }

    @Test
    fun cacheEvictionBoundsNativeAssetsAndFailuresAreNotCached() {
        var reads = 0
        val cache = FileIconCache(2) { reads++; if (it == "broken") "" else it }
        for (key in listOf("pdf", "txt", "folder", "pdf")) cache.get(key)
        assertEquals(4, reads)
        repeat(2) { assertFailsWith<IllegalStateException> { cache.get("broken") } }
        assertEquals(6, reads)
    }
}
