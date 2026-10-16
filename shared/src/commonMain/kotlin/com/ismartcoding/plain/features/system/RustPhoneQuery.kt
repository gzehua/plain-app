package com.ismartcoding.plain.features.system

import kotlin.time.Instant

import com.ismartcoding.plain.graphql.models.Call as PhoneCall
import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.content.gql
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.data.*
import com.ismartcoding.plain.features.contact.*
import kotlinx.serialization.json.*

internal object RustPhoneQuery {
    suspend fun contacts(query: String, limit: Int, offset: Int): List<com.ismartcoding.plain.graphql.models.Contact> {
        val fields = "id prefix firstName middleName lastName suffix nickname photoId phoneNumbers { value type typeCode label normalizedNumber } emails { value type typeCode label } addresses { value type typeCode label } events { value type typeCode label } websites { value type typeCode label } ims { value protocol customProtocol typeCode } source starred contactId thumbnailId notes groups { id name } organization { company title } ringtone updatedAt"
        return JsonHelper.jsonDecodeFromElement(PlainRust.mainQuery("contacts(query: ${gql(query)}, limit: $limit, offset: $offset) { $fields }").getValue("contacts"))
    }
    suspend fun calls(query: String, limit: Int, offset: Int): List<PhoneCall> =
        JsonHelper.jsonDecodeFromElement(PlainRust.mainQuery("calls(query: ${gql(query)}, limit: $limit, offset: $offset) { id number name photoId photoUri startedAt durationSec type typeCode accountId }").getValue("calls"))
    suspend fun countContacts(query: String): Int = PlainRust.mainQuery("contactCount(query: ${gql(query)})").getValue("contactCount").jsonPrimitive.int
    suspend fun countCalls(query: String): Int = PlainRust.mainQuery("callCount(query: ${gql(query)})").getValue("callCount").jsonPrimitive.int
    suspend fun contactIds(query: String): Set<String> = PlainRust.mainQuery("contactIds(query: ${gql(query)})").getValue("contactIds").jsonArray.map { it.jsonPrimitive.content }.toSet()
    suspend fun callIds(query: String): Set<String> = PlainRust.mainQuery("callIds(query: ${gql(query)})").getValue("callIds").jsonArray.map { it.jsonPrimitive.content }.toSet()
}
