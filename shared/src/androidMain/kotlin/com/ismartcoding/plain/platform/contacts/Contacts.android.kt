package com.ismartcoding.plain.platform.contacts

import com.ismartcoding.plain.platform.permissions.isGranted

import com.ismartcoding.plain.models.ContactGroup
import com.ismartcoding.plain.appContext
import com.ismartcoding.plain.platform.media.provider.ContactMediaStoreHelper
import com.ismartcoding.plain.platform.permissions.Permission
import com.ismartcoding.plain.rust.host.provider.ContactGroupFacts
import com.ismartcoding.plain.rust.host.provider.ContactPageFacts
import com.ismartcoding.plain.rust.host.provider.ContactSourceFacts
import com.ismartcoding.plain.rust.host.provider.ProviderPlan

internal actual fun getContactGroups(): List<ContactGroupFacts> = GroupHelper.getAll()

actual fun createContactGroup(name: String, accountName: String, accountType: String): ContactGroup =
    GroupHelper.create(name, accountName, accountType)

actual fun updateContactGroup(id: String, name: String): ContactGroup? {
    GroupHelper.update(id, name)
    return ContactGroup(id, name)
}

actual fun deleteContactGroup(id: String) = GroupHelper.delete(id)

internal actual fun getContactSources(): List<ContactSourceFacts> = SourceHelper.getAll()

internal actual suspend fun readContacts(plan: ProviderPlan, limit: Int, offset: Int): ContactPageFacts {
    if (!Permission.READ_CONTACTS.isGranted()) return ContactPageFacts()
    return ContactMediaStoreHelper.searchAsync(appContext, plan.where(), limit, offset)
}
