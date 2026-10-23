package com.ismartcoding.plain.platform.contacts

import com.ismartcoding.plain.models.ContactGroup
import com.ismartcoding.plain.rust.host.provider.ContactGroupFacts
import com.ismartcoding.plain.rust.host.provider.ContactPageFacts
import com.ismartcoding.plain.rust.host.provider.ContactSourceFacts
import com.ismartcoding.plain.rust.host.provider.ProviderPlan

internal actual fun getContactGroups(): List<ContactGroupFacts> = emptyList()

actual fun createContactGroup(name: String, accountName: String, accountType: String): ContactGroup =
    ContactGroup("0", name)

actual fun updateContactGroup(id: String, name: String): ContactGroup? = null

actual fun deleteContactGroup(id: String) {}

internal actual fun getContactSources(): List<ContactSourceFacts> = emptyList()

internal actual suspend fun readContacts(plan: ProviderPlan, limit: Int, offset: Int): ContactPageFacts = ContactPageFacts()
