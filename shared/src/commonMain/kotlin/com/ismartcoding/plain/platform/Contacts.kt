package com.ismartcoding.plain.platform

import com.ismartcoding.plain.graphql.models.ContactSource
import com.ismartcoding.plain.graphql.models.ContactGroup

/**
 * Returns all contact groups. Empty list on platforms without a contacts provider.
 */
expect fun getContactGroups(): List<ContactGroup>

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
expect fun getContactSources(): List<ContactSource>
