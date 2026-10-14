package com.ismartcoding.plain.ui.extensions

import com.ismartcoding.plain.features.file.DFile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class FilePreviewMediaIdTest {
    @Test
    fun file_preview_uses_system_identity_and_leaves_unindexed_files_unidentified() {
        val file = DFile("one.mp4", "/movies/one.mp4", "rw", null, Instant.fromEpochMilliseconds(0), 1L, false, 0, "42")
        assertEquals("42", file.toPreviewItem().mediaId)
        assertEquals("", file.copy(mediaId = "").toPreviewItem().mediaId)
        assertEquals(file.path, file.toPreviewItem().id)
    }
}
