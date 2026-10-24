package com.ismartcoding.plain.platform.system

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class BackgroundExecutionTest {
    @Test
    fun cancellation_before_first_dispatch_still_completes_once_and_releases_the_id() = runTest {
        val results = mutableListOf<Boolean>()
        var polled = 0
        val id = "background-before-dispatch"
        val job = BackgroundExecution.launchExecution(id, this, results::add) { polled++; true }
        BackgroundExecution.cancel(id)
        runCurrent()
        assertTrue(job.isCancelled)
        assertEquals(0, polled)
        assertEquals(listOf(false), results)
        BackgroundExecution.cancel(id)
        runCurrent()
        assertEquals(listOf(false), results)
        BackgroundExecution.launchExecution(id, this, results::add) { true }
        runCurrent()
        assertEquals(listOf(false, true), results)
    }

    @Test
    fun platform_completion_follows_owned_cleanup() = runTest {
        val order = mutableListOf<String>()
        val id = "background-cleanup-order"
        BackgroundExecution.launchExecution(id, this, { success -> order.add("completed:$success") }) {
            try { awaitCancellation() } finally { order.add("cleanup") }
        }
        runCurrent()
        BackgroundExecution.cancel(id)
        runCurrent()
        assertEquals(listOf("cleanup", "completed:false"), order)
    }
    @Test
    fun cancellation_between_grant_delivery_and_acknowledgement_waits_for_revocation() = runTest {
        val acknowledgement = CompletableDeferred<Unit>()
        val cleanup = CompletableDeferred<Unit>()
        val order = mutableListOf<String>()
        val job = launch {
            BackgroundExecution.withGrant(
                grant = { order.add("granted"); acknowledgement.await(); order.add("acknowledged") },
                awaitCompletion = { order.add("waited"); true },
                revoke = { order.add("revoked"); cleanup.await(); order.add("released") },
            )
        }
        runCurrent()
        job.cancel()
        runCurrent()
        assertEquals(listOf("granted"), order)
        acknowledgement.complete(Unit)
        runCurrent()
        assertEquals(listOf("granted", "acknowledged", "revoked"), order)
        assertTrue(!job.isCompleted)
        cleanup.complete(Unit)
        runCurrent()
        assertTrue(job.isCompleted)
        assertEquals(listOf("granted", "acknowledged", "revoked", "released"), order)
    }

    @Test
    fun failed_grant_acknowledgement_still_releases_a_possibly_accepted_grant() = runTest {
        var revoked = 0
        var waited = 0
        try {
            BackgroundExecution.withGrant(
                grant = { error("acknowledgement lost") },
                awaitCompletion = { waited++; true },
                revoke = { revoked++ },
            )
            error("Grant failure was hidden")
        } catch (error: IllegalStateException) {
            assertEquals("acknowledgement lost", error.message)
        }
        assertEquals(1, revoked)
        assertEquals(0, waited)
    }

}
