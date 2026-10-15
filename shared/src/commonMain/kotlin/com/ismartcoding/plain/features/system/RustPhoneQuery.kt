package com.ismartcoding.plain.features.system

import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.content.gql
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.data.*
import com.ismartcoding.plain.features.contact.*
import kotlinx.serialization.json.*
import kotlin.time.Instant

internal object RustPhoneQuery {
    suspend fun contacts(query: String, limit: Int, offset: Int): List<DContact> {
        val fields = "id prefix firstName middleName lastName suffix nickname photoId phoneNumbers { value type: typeCode label normalizedNumber } emails { value type: typeCode label } addresses { value type: typeCode label } events { value type: typeCode label } websites { value type: typeCode label } ims { value type: typeCode label: customProtocol } source starred contactId thumbnailId notes groups { id name } organization { company title } ringtone updatedAt"
        return JsonHelper.jsonDecodeFromElement<List<ContactFacts>>(PlainRust.mainQuery("contacts(query: ${gql(query)}, limit: $limit, offset: $offset) { $fields }").getValue("contacts")).map { row ->
            fun details(items: List<ContactDetailFacts>) = items.map { DContentItem(it.value, it.type, it.label) }
            DContact(row.id, row.prefix, row.firstName, row.middleName, row.lastName, row.suffix, row.nickname, row.photoId,
                row.phoneNumbers.map { DContactPhoneNumber(it.value, it.type, it.label, it.normalizedNumber) },
                details(row.emails), details(row.addresses), details(row.events), row.source, if (row.starred) 1 else 0,
                row.contactId, row.thumbnailId, row.notes, row.groups.map { DGroup(it.id.toLong(), it.name) },
                row.organization?.let { DOrganization(it.company, it.title) }, details(row.websites), details(row.ims), row.ringtone, Instant.parse(row.updatedAt))
        }
    }
    suspend fun calls(query: String, limit: Int, offset: Int): List<DCall> =
        JsonHelper.jsonDecodeFromElement<List<CallFacts>>(PlainRust.mainQuery("calls(query: ${gql(query)}, limit: $limit, offset: $offset) { id number name photoUri startedAt durationSec type: typeCode accountId }").getValue("calls"))
            .map { DCall(it.id, it.number, it.name, it.photoUri, Instant.parse(it.startedAt), it.durationSec, it.type, it.accountId) }
    suspend fun countContacts(query: String): Int = PlainRust.mainQuery("contactCount(query: ${gql(query)})").getValue("contactCount").jsonPrimitive.int
    suspend fun countCalls(query: String): Int = PlainRust.mainQuery("callCount(query: ${gql(query)})").getValue("callCount").jsonPrimitive.int
    suspend fun contactIds(query: String): Set<String> = PlainRust.mainQuery("contactIds(query: ${gql(query)})").getValue("contactIds").jsonArray.map { it.jsonPrimitive.content }.toSet()
    suspend fun callIds(query: String): Set<String> = PlainRust.mainQuery("callIds(query: ${gql(query)})").getValue("callIds").jsonArray.map { it.jsonPrimitive.content }.toSet()
}
