package com.ismartcoding.plain.platform.contacts

import com.ismartcoding.plain.platform.permissions.isGranted

import com.ismartcoding.plain.models.ContactGroup

internal actual fun getContactGroups(): List<com.ismartcoding.plain.rust.host.provider.ContactGroupFacts> = GroupHelper.getAll()

actual fun createContactGroup(name: String, accountName: String, accountType: String): ContactGroup =
    GroupHelper.create(name, accountName, accountType)

actual fun updateContactGroup(id: String, name: String): ContactGroup? {
    GroupHelper.update(id, name)
    return ContactGroup(id, name)
}

actual fun deleteContactGroup(id: String) = GroupHelper.delete(id)

internal actual fun getContactSources(): List<com.ismartcoding.plain.rust.host.provider.ContactSourceFacts> = SourceHelper.getAll()

internal actual suspend fun readContacts(plan: com.ismartcoding.plain.rust.host.provider.ProviderPlan, limit: Int, offset: Int): com.ismartcoding.plain.rust.host.provider.ContactPageFacts {
    if (!com.ismartcoding.plain.platform.permissions.Permission.READ_CONTACTS.isGranted()) return com.ismartcoding.plain.rust.host.provider.ContactPageFacts()
    return com.ismartcoding.plain.platform.media.provider.ContactMediaStoreHelper.searchAsync(com.ismartcoding.plain.appContext, plan.where(), limit, offset)
}
