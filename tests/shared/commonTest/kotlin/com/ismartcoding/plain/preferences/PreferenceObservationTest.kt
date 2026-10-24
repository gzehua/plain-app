package com.ismartcoding.plain.preferences

import com.ismartcoding.plain.rust.native.nativeValue
import kotlinx.serialization.SerializationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PreferenceObservationTest {
    @Test
    fun notificationsReadTheCurrentValueEvenWhenAnOlderNotificationArrivesLater() {
        var stored = 3
        val key = "test_current_value"
        val state = Prefs.observe(key) { stored }
        assertEquals(3, state.value)
        stored = 4
        assertEquals(setOf(key), Prefs.refresh(setOf(key)))
        assertEquals(4, state.value)
        stored = 5
        assertEquals(setOf(key), Prefs.refresh(setOf(key)))
        assertEquals(5, state.value)
        assertEquals(emptySet(), Prefs.refresh(setOf(key)))
    }

    @Test
    fun malformedNativeValuesFailInsteadOfSilentlyReturningAnOldValue() {
        var reply = """{"value":3}"""
        val key = "test_strict_value"
        val state = Prefs.observe(key) { nativeValue<Int>(reply) }
        reply = """{"value":"invalid"}"""
        assertFailsWith<SerializationException> { Prefs.refresh(setOf(key)) }
        assertEquals(3, state.value)
        reply = """{"value":8}"""
        assertEquals(setOf(key), Prefs.refresh(setOf(key)))
        assertEquals(8, state.value)
    }

    @Test
    fun unrelatedKeysDoNotReadObservedPreferences() {
        var reads = 0
        val key = "test_read_selection"
        Prefs.observe(key) { reads++; 7 }
        assertEquals(1, reads)
        assertEquals(emptySet(), Prefs.refresh(setOf("test_other_key")))
        assertEquals(1, reads)
        assertFailsWith<IllegalStateException> { Prefs.observe(key) { reads++; 8 } }
        assertEquals(1, reads)
    }
}
