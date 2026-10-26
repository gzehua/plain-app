package com.ismartcoding.plain.rust

import com.ismartcoding.plain.events.EventType
import com.ismartcoding.plain.features.images.search.RustImageModels
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertSame

class ImageIndexEventsTest {
    @Test fun raw_index_progress_does_not_query_a_core_or_replace_the_model_snapshot() = runTest {
        val before = RustImageModels.snapshot.value
        repeat(32) { indexed ->
            RustEventHandler.handleEvent(RustEvent(
                EventType.IMAGE_INDEX_UPDATED,
                """{"version":1,"isRunning":true,"totalImages":64,"indexedImages":$indexed,"skippedImages":0,"errorMessage":""}""",
            ))
        }
        assertSame(before, RustImageModels.snapshot.value)
    }
}
