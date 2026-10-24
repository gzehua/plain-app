package com.ismartcoding.plain.features.nearby

import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class DiscoverySessionTest {
    @Test fun leaving_view_releases_exactly_its_acquired_session() = runTest {
        val calls = mutableListOf<String>()
        val acquired = CompletableDeferred<Unit>()
        val job = launch { discoverySession("view-one", { calls.add("acquire:$it"); acquired.complete(Unit) }, { calls.add("release:$it") }) }
        acquired.await()
        assertEquals(listOf("acquire:view-one"), calls)
        job.cancelAndJoin()
        assertEquals(listOf("acquire:view-one", "release:view-one"), calls)
    }

    @Test fun an_acquisition_error_still_releases_the_possibly_committed_request() = runTest {
        val released = mutableListOf<String>()
        assertFailsWith<IllegalStateException> {
            discoverySession("view-one", { error("Fixture lost reply") }, { released.add(it) })
        }
        assertEquals(listOf("view-one"), released)
    }

    @Test fun cancellation_waits_for_a_blocking_acquisition_before_release() = runTest {
        val entered = CompletableDeferred<Unit>()
        val complete = CompletableDeferred<Unit>()
        val released = mutableListOf<String>()
        val job = launch {
            discoverySession("view-one", {
                entered.complete(Unit)
                withContext(NonCancellable) { complete.await() }
            }, { released.add(it) })
        }
        entered.await()
        job.cancel()
        runCurrent()
        assertTrue(released.isEmpty())
        complete.complete(Unit)
        job.join()
        assertEquals(listOf("view-one"), released)
    }
}
