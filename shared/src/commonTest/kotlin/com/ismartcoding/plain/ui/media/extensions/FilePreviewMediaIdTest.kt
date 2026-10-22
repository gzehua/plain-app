package com.ismartcoding.plain.ui.media.extensions


import com.ismartcoding.plain.models.File as FileEntry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class FilePreviewMediaIdTest {
    @Test
    fun file_preview_uses_system_identity_and_leaves_unindexed_files_unidentified() {
        val file = FileEntry(name = "one.mp4", path = "/movies/one.mp4", permission = "rw", createdAt = null, updatedAt = Instant.fromEpochMilliseconds(0), size = 1L, isDir = false, childCount = 0, mediaId = "42")
        assertEquals("42", file.toPreviewItem().mediaId)
        assertEquals("", file.copy(mediaId = "").toPreviewItem().mediaId)
        assertEquals("", file.copy(mediaId = null).toPreviewItem().mediaId)
        assertEquals(file.path, file.toPreviewItem().id)
    }
}
