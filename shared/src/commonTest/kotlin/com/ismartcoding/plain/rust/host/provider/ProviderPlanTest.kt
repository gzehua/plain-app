package com.ismartcoding.plain.rust.host.provider

import com.ismartcoding.plain.lib.JsonHelper
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProviderPlanTest {
    @Test
    fun executes_prepared_values_without_interpreting_the_query() {
        val plan = JsonHelper.jsonDecode<ProviderPlan>("""{"clauses":["title = ?"],"args":["quote'_%"],"idsColumn":"_id","ids":["7","9"],"trash":true}""")
        val where = plan.where()
        assertEquals("title = ? AND _id IN (?,?)", where.toSelection())
        assertEquals(listOf("quote'_%", "7", "9"), where.args)
        assertEquals(true, where.trash)
    }

    @Test
    fun count_chunks_keep_conditions_arguments_and_trash() {
        val ids = (1..4001).map(Int::toString)
        val chunks = ProviderPlan(listOf("title = ?"), listOf("name"), "_id", ids, false).wheres()
        assertEquals(3, chunks.size)
        assertEquals(ids, chunks.flatMap { it.args.drop(1) })
        chunks.forEach { where ->
            assertEquals("name", where.args.first())
            assertEquals(false, where.trash)
            assertTrue(where.toSelection().startsWith("title = ? AND _id IN ("))
            assertFalse(where.args.size > 2001)
        }
    }

    @Test
    fun empty_id_plan_executes_once_and_preserves_null_trash() {
        val chunks = ProviderPlan().wheres()
        assertEquals(1, chunks.size)
        assertEquals("1=1", chunks.single().toSelection())
        assertEquals(null, chunks.single().trash)
    }
}
