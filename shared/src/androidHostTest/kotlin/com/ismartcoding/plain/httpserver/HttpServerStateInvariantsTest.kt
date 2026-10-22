package com.ismartcoding.plain.httpserver

import com.ismartcoding.plain.app.HttpServerManager

import com.ismartcoding.plain.features.settings.HttpServerState
import com.ismartcoding.plain.app.startHttpServerAsync
import com.ismartcoding.plain.app.refreshHttpServerState
import kotlinx.coroutines.runBlocking
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Regression locks for the 2026-09 "QQ-switch spinner" bug class: the server
 * state must never be stranded in a processing state (STARTING) —
 * the home card maps those to an endless loading spinner. The original bug:
 * STARTING was written as a UI intent before the service even spawned; when
 * the start command was lost (EMUI blocked the foreground-service start as
 * the app went to background), no writer ever followed and the spinner never
 * ended.
 *
 * The start orchestrator records a terminal state even when the engine
 * cannot be created at all (host: no Android context → the create throws).
 * iOS previously had no catch anywhere and would strand STARTING.
 */
class HttpServerStateInvariantsTest {

    @AfterTest
    fun reset() {
        HttpServerManager.httpServerError.value = ""
        HttpServerManager.serverState.value = HttpServerState.OFF
    }

    @Test
    fun startOrchestratorRecordsTerminalStateWhenEngineCreateFails() = runBlocking {
        // Native initialization is unavailable in the host environment.
        startHttpServerAsync()

        assertEquals(
            HttpServerState.ERROR,
            HttpServerManager.serverState.value,
            "an engine-create failure must land ERROR, never strand STARTING (QQ-switch spinner bug)",
        )
        assertTrue(HttpServerManager.httpServerError.value.isNotEmpty(), "failure reason must be recorded for the error card")
    }

    @Test
    fun statusRefreshBeforeCoreInitializationKeepsStoppedState() = runBlocking {
        HttpServerManager.serverState.value = HttpServerState.OFF
        refreshHttpServerState()
        assertEquals(HttpServerState.OFF, HttpServerManager.serverState.value)
        assertEquals("", HttpServerManager.httpServerError.value)
    }
}
