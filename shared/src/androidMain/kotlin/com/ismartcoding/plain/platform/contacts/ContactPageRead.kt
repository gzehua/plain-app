package com.ismartcoding.plain.platform.contacts

import com.ismartcoding.plain.rust.host.provider.ContactFacts
import com.ismartcoding.plain.rust.host.provider.ContactDataFacts
import com.ismartcoding.plain.rust.host.provider.ContactGroupFacts
import com.ismartcoding.plain.rust.host.provider.ContactPageFacts

internal fun readContactPage(
    readRows: () -> List<ContactFacts>,
    readDetails: (List<String>) -> List<ContactDataFacts>,
    readGroups: () -> List<ContactGroupFacts>,
): ContactPageFacts {
    val rows = readRows()
    if (rows.isEmpty()) return ContactPageFacts()
    return ContactPageFacts(rows, readDetails(rows.map { it.id }), readGroups())
}
