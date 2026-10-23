package com.ismartcoding.plain.platform.contacts

import com.ismartcoding.plain.models.ContactGroup
import com.ismartcoding.plain.rust.host.provider.ContactGroupFacts
import com.ismartcoding.plain.rust.host.provider.ContactPageFacts
import com.ismartcoding.plain.rust.host.provider.ContactSourceFacts
import com.ismartcoding.plain.rust.host.provider.ProviderPlan

/**
 * Returns all contact groups. Empty list on platforms without a contacts provider.
 */
internal expect fun getContactGroups(): List<ContactGroupFacts>

/**
 * Create a new contact group with [name] under the account identified by
 * [accountName]/[accountType]. Returns the newly created group.
 */
expect fun createContactGroup(name: String, accountName: String, accountType: String): ContactGroup

/**
 * Update the contact group identified by [id], changing its name to [name].
 * Returns the updated group, or null on failure / unsupported platforms.
 */
expect fun updateContactGroup(id: String, name: String): ContactGroup?

/**
 * Delete the contact group identified by [id]. No-op on unsupported platforms.
 */
expect fun deleteContactGroup(id: String)

/**
 * Returns all contact sources (accounts). Empty list on unsupported platforms.
 */
internal expect fun getContactSources(): List<ContactSourceFacts>

internal expect suspend fun readContacts(plan: ProviderPlan, limit: Int, offset: Int): ContactPageFacts
