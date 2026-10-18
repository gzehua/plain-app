package com.ismartcoding.plain.rust.request

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class NativeManagementLifecycleTest {
    @Test
    fun bridgeCallsDoNotInitializeTheRuntime() = runBlocking {
        var requests = 0
        val result = NativeManagementClient.call<Boolean> {
            requests++
            """{"value":true}"""
        }
        assertTrue(result)
        assertEquals(1, requests)
    }

    @Test
    fun bridgeErrorsReachTheCallerWithoutRestartingTheRuntime() = runBlocking {
        val failure = assertFailsWith<IllegalStateException> {
            NativeManagementClient.call<Boolean> { """{"error":"Core is not initialized"}""" }
        }
        assertEquals("Core is not initialized", failure.message)
    }
}
