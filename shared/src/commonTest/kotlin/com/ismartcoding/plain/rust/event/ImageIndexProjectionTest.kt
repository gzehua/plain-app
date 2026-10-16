package com.ismartcoding.plain.rust.event

import com.ismartcoding.plain.app.events.EventType
import com.ismartcoding.plain.features.images.search.RustImageModels
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertSame

class ImageIndexProjectionTest {
    @Test fun raw_index_progress_does_not_query_a_core_or_replace_the_model_snapshot() = runTest {
        val before = RustImageModels.snapshot.value
        repeat(32) { indexed ->
            RustEventProjection.apply(RustEvent(
                EventType.IMAGE_INDEX_UPDATED.name,
                """{"version":1,"isRunning":true,"totalImages":64,"indexedImages":$indexed,"skippedImages":0,"errorMessage":""}""",
            ))
        }
        assertSame(before, RustImageModels.snapshot.value)
    }
}
