package com.ismartcoding.plain.rust.host

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class HostRequestDispatchTest {
    @Test
    fun state_callbacks_enter_the_projection_in_receipt_order_even_when_the_first_suspends() = runTest {
        val gate = CompletableDeferred<Unit>()
        val mutex = Mutex()
        val applied = mutableListOf<String>()
        launchHostRequest("feedSyncChanged") {
            mutex.withLock {
                applied += "snapshot"
                gate.await()
                applied += "snapshot-complete"
            }
        }
        launchHostRequest("feedSyncChanged") { mutex.withLock { applied += "delta" } }
        assertEquals(listOf("snapshot"), applied)
        gate.complete(Unit)
        runCurrent()
        assertEquals(listOf("snapshot", "snapshot-complete", "delta"), applied)
    }

    @Test
    fun peer_projection_callbacks_complete_in_order_while_platform_operations_remain_dispatched() = runTest {
        val applied = mutableListOf<String>()
        launchHostRequest("peerTransportsChanged") { applied += "old" }
        launchHostRequest("peerTransportsChanged") { applied += "current" }
        launchHostRequest("systemCapabilities") { applied += "platform" }
        assertEquals(listOf("old", "current"), applied)
        runCurrent()
        assertEquals(listOf("old", "current", "platform"), applied)
    }
}
