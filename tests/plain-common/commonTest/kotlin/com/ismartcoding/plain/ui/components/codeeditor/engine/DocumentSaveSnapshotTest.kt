package com.ismartcoding.plain.ui.components.codeeditor.engine

import kotlin.test.*

class DocumentSaveSnapshotTest {
    @Test fun streamed_save_uses_captured_lines_even_when_live_document_changes() {
        val document = LineVectorDocument.fromText("first\nsecond")
        val saved = document.snapshot()
        val chunks = document.byteChunks(saved, chunkSize = 3)
        document.replace(EditRange(0, 0, 0, 5), "changed")
        val bytes = chunks.asSequence().fold(ByteArray(0)) { result, chunk -> result + chunk }
        assertContentEquals("first\nsecond".encodeToByteArray(), bytes)
        assertEquals("changed", document.lineText(0))
        assertNotEquals(saved.version, document.version)
    }
}
