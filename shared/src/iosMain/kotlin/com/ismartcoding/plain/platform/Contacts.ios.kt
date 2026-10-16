package com.ismartcoding.plain.platform

import com.ismartcoding.plain.graphql.models.ContactSource
import com.ismartcoding.plain.graphql.models.ContactGroup

actual fun getContactGroups(): List<ContactGroup> = emptyList()

actual fun createContactGroup(name: String, accountName: String, accountType: String): ContactGroup =
    ContactGroup("0", name)

actual fun updateContactGroup(id: String, name: String): ContactGroup? = null

actual fun deleteContactGroup(id: String) {}

actual fun getContactSources(): List<ContactSource> = emptyList()
