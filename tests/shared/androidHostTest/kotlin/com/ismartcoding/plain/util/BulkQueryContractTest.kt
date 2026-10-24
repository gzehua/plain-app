package com.ismartcoding.plain.util

import com.ismartcoding.plain.helpers.SearchHelper

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Locks the parsing shapes the empty-query bulk guard depends on:
 * a blank query parses to nothing anywhere, and `all:true` survives the whole
 * pipeline as a named sentinel field (API_SPEC §5).
 */
class BulkQueryContractTest {

    @Test
    fun emptyQueryParsesToNothing() {
        assertEquals(emptyList(), SearchHelper.parse(""))
    }

    @Test
    fun allSentinelParsesAsNamedField() {
        val fields = SearchHelper.parse("all:true")
        assertEquals(1, fields.size)
        assertEquals("all", fields[0].name)
        assertEquals("true", fields[0].value)
    }

    @Test
    fun allSentinelDoesNotBecomeATextSearch() {
        // A bare word is a text search; only `all:<value>` is the sentinel.
        val bare = SearchHelper.parse("all")
        assertEquals("text", bare[0].name)
        assertEquals("all", bare[0].value)
    }
}
