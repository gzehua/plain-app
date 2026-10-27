package com.ismartcoding.plain.rust

import com.ismartcoding.plain.features.images.search.RustImageModels
import com.ismartcoding.plain.features.images.search.ImageModelsSnapshot
import kotlin.test.Test
import kotlin.test.assertEquals

class ImageIndexEventsTest {
    @Test fun older_model_snapshots_cannot_replace_newer_state() {
        val revision = RustImageModels.snapshot.value.version + 2
        val current = ImageModelsSnapshot(version = revision, indexedImages = 64)
        RustImageModels.apply(current)
        RustImageModels.apply(ImageModelsSnapshot(version = revision - 1, indexedImages = 1))
        assertEquals(current, RustImageModels.snapshot.value)
    }
}
