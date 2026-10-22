package com.ismartcoding.plain.platform.contacts

import com.ismartcoding.plain.models.ContactGroup

internal actual fun getContactGroups(): List<com.ismartcoding.plain.rust.host.provider.ContactGroupFacts> = emptyList()

actual fun createContactGroup(name: String, accountName: String, accountType: String): ContactGroup =
    ContactGroup("0", name)

actual fun updateContactGroup(id: String, name: String): ContactGroup? = null

actual fun deleteContactGroup(id: String) {}

internal actual fun getContactSources(): List<com.ismartcoding.plain.rust.host.provider.ContactSourceFacts> = emptyList()

internal actual suspend fun readContacts(plan: com.ismartcoding.plain.rust.host.provider.ProviderPlan, limit: Int, offset: Int): com.ismartcoding.plain.rust.host.provider.ContactPageFacts = com.ismartcoding.plain.rust.host.provider.ContactPageFacts()
