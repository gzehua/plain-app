package com.ismartcoding.plain.rust.native

import com.ismartcoding.plain.Prefs
import com.ismartcoding.plain.rust.RustRuntime
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class NativeManagementLifecycleTest {
    @Test
    fun preferencesCannotLoadReadOrWriteBeforeCoreStartup() = runBlocking {
        assertFalse(RustRuntime.isCoreInitialized)
        assertEquals("Rust core must start before loading preferences", assertFailsWith<IllegalStateException> {
            Prefs.load()
        }.message)
        assertEquals("Rust core must start before reading preferences", assertFailsWith<IllegalStateException> {
            Prefs.get<Int>("http_port")
        }.message)
        assertEquals("Rust core must start before writing preferences", assertFailsWith<IllegalStateException> {
            Prefs.set("http_port", 8080)
        }.message)
        assertFalse(RustRuntime.isCoreInitialized)
    }

    @Test
    fun bridgeCallsDoNotInitializeTheRuntime() = runBlocking {
        var requests = 0
        val result = nativeCall<Boolean> {
            requests++
            """{"value":true}"""
        }
        assertTrue(result)
        assertEquals(1, requests)
    }

    @Test
    fun bridgeErrorsReachTheCallerWithoutRestartingTheRuntime() = runBlocking {
        val failure = assertFailsWith<IllegalStateException> {
            nativeCall<Boolean> { """{"error":"Core is not initialized"}""" }
        }
        assertEquals("Core is not initialized", failure.message)
    }
}
