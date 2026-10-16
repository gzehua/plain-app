package com.ismartcoding.plain.platform

import com.ismartcoding.plain.graphql.models.ContactSource
import com.ismartcoding.plain.graphql.models.ContactGroup
import com.ismartcoding.plain.features.contact.GroupHelper
import com.ismartcoding.plain.features.contact.SourceHelper

actual fun getContactGroups(): List<ContactGroup> = GroupHelper.getAll()

actual fun createContactGroup(name: String, accountName: String, accountType: String): ContactGroup =
    GroupHelper.create(name, accountName, accountType)

actual fun updateContactGroup(id: String, name: String): ContactGroup? {
    GroupHelper.update(id, name)
    return ContactGroup(id, name)
}

actual fun deleteContactGroup(id: String) = GroupHelper.delete(id)

actual fun getContactSources(): List<ContactSource> = SourceHelper.getAll()
