package com.ismartcoding.plain.rust.host.audio

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class AudioCatalogHostTest {
    @Test
    fun callback_bursts_keep_only_one_pending_fact_and_cancellation_closes_the_source() = runTest {
        val source = MutableSharedFlow<Unit>(extraBufferCapacity = 1024)
        var closed = false
        val changes = callbackFlow {
            val forwarder = launch { source.collect { trySend(Unit) } }
            awaitClose { forwarder.cancel(); closed = true }
        }
        val gate = CompletableDeferred<Unit>()
        var reports = 0
        val observer = launch {
            forwardAudioCatalogChanges(changes) { reports++; if (reports == 1) gate.await() }
        }
        runCurrent()
        source.emit(Unit)
        runCurrent()
        assertEquals(1,reports)
        repeat(1000) { source.emit(Unit) }
        runCurrent()
        assertEquals(1,reports)
        gate.complete(Unit)
        runCurrent()
        assertEquals(2,reports)
        observer.cancelAndJoin()
        assertTrue(closed)
        source.emit(Unit)
        runCurrent()
        assertEquals(2,reports)
    }
}
