package com.ismartcoding.plain.platform.contacts

import com.ismartcoding.plain.rust.host.provider.ContactFacts
import com.ismartcoding.plain.rust.host.provider.ContactDataFacts
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ContactPageReadTest {
    @Test
    fun emptyProviderPageDoesNotReadDetailsOrGroups() {
        val page = readContactPage({ emptyList() }, { fail("empty page read details") }, { fail("empty page read groups") })
        assertEquals(emptyList(), page.rows)
    }

    @Test
    fun providerBatchReadsOnlyTheCurrentPageOnceAndInOrder() {
        val calls = mutableListOf<String>()
        val rows = listOf("b", "a").map { ContactFacts(it, "", "", "", "", "", "", "", false, "", "", "", "") }
        val detail = ContactDataFacts("a", "vnd.android.cursor.item/note", "text", "", "", "", "", "")
        val page = readContactPage(
            { calls.add("page"); rows },
            { ids -> calls.add("details"); assertEquals(listOf("b", "a"), ids); listOf(detail) },
            { calls.add("groups"); emptyList() },
        )
        assertEquals(listOf("page", "details", "groups"), calls)
        assertEquals(rows, page.rows)
        assertEquals(listOf(detail), page.details)
    }
}
