package com.ismartcoding.plain.rust.graphql

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SubscriptionUpdateTest {
    @Test
    fun filtered_changes_emit_attachments_and_matching_data() = runTest {
        val filteredValues = mutableListOf<Int>()
        val updates = flowOf(
            SubscriptionUpdate.Attached,
            SubscriptionUpdate.Data(1),
            SubscriptionUpdate.Data(2),
            SubscriptionUpdate.Failed(IllegalStateException("Subscription failed")),
            SubscriptionUpdate.Attached,
        )

        val changes = updates.changes { value ->
            filteredValues.add(value)
            value == 2
        }.toList()

        assertEquals(listOf(Unit, Unit, Unit), changes)
        assertEquals(listOf(1, 2), filteredValues)
    }

    @Test
    fun unfiltered_changes_emit_attachments_and_all_data() = runTest {
        val updates = flowOf(
            SubscriptionUpdate.Attached,
            SubscriptionUpdate.Data(1),
            SubscriptionUpdate.Data(2),
            SubscriptionUpdate.Failed(IllegalStateException("Subscription failed")),
        )

        assertEquals(listOf(Unit, Unit, Unit), updates.changes().toList())
    }
}
