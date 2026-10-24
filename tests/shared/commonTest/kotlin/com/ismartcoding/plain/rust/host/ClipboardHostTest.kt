package com.ismartcoding.plain.rust.host

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class ClipboardHostTest {
    @Test
    fun settings_changes_and_host_shutdown_own_exactly_one_observation() = runTest {
        val enabled = MutableStateFlow(false)
        var starts = 0
        var stops = 0
        var active = 0
        val host = launch {
            observeClipboardWhenEnabled(enabled) {
                starts++
                active++
                assertEquals(1, active)
                try { awaitCancellation() }
                finally { active--; stops++ }
            }
        }
        runCurrent()
        assertEquals(0, starts)
        enabled.value = true
        runCurrent()
        enabled.value = true
        runCurrent()
        assertEquals(1, starts)
        enabled.value = false
        runCurrent()
        assertEquals(1, stops)
        enabled.value = true
        runCurrent()
        assertEquals(2, starts)
        host.cancelAndJoin()
        assertEquals(2, stops)
        assertEquals(0, active)
        enabled.value = true
        runCurrent()
        assertEquals(2, starts)
    }
}
