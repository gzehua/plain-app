package com.ismartcoding.plain.rust.host.provider

import kotlin.time.Instant

import com.ismartcoding.plain.api.graphql.models.Call as PhoneCall
import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.request.gql
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.data.*
import com.ismartcoding.plain.features.files.*
import com.ismartcoding.plain.ui.files.*
import com.ismartcoding.plain.features.notifications.*
import com.ismartcoding.plain.features.screenmirror.*
import com.ismartcoding.plain.features.settings.*
import com.ismartcoding.plain.ui.settings.*
import com.ismartcoding.plain.features.tags.*
import com.ismartcoding.plain.ui.tags.*
import com.ismartcoding.plain.platform.files.*
import com.ismartcoding.plain.ui.platform.files.*
import com.ismartcoding.plain.ui.state.*
import com.ismartcoding.plain.app.*
import com.ismartcoding.plain.ui.app.*
import com.ismartcoding.plain.features.files.stored.*
import com.ismartcoding.plain.ui.files.stored.*
import com.ismartcoding.plain.platform.files.*
import com.ismartcoding.plain.ui.platform.files.*
import com.ismartcoding.plain.platform.media.*
import com.ismartcoding.plain.ui.platform.media.*
import com.ismartcoding.plain.platform.notifications.*
import com.ismartcoding.plain.ui.platform.notifications.*
import com.ismartcoding.plain.platform.system.*
import com.ismartcoding.plain.ui.platform.system.*
import com.ismartcoding.plain.ui.format.*
import com.ismartcoding.plain.util.*
import com.ismartcoding.plain.features.permissions.*
import com.ismartcoding.plain.ui.permissions.*
import com.ismartcoding.plain.features.settings.*
import com.ismartcoding.plain.ui.settings.*
import com.ismartcoding.plain.app.*
import com.ismartcoding.plain.ui.app.*
import com.ismartcoding.plain.ui.navigation.*
import com.ismartcoding.plain.features.sessions.*
import com.ismartcoding.plain.ui.sessions.*
import com.ismartcoding.plain.features.settings.*
import com.ismartcoding.plain.ui.settings.*
import com.ismartcoding.plain.platform.files.*
import com.ismartcoding.plain.ui.platform.files.*
import com.ismartcoding.plain.platform.media.*
import com.ismartcoding.plain.ui.platform.media.*
import com.ismartcoding.plain.platform.notifications.*
import com.ismartcoding.plain.ui.platform.notifications.*
import com.ismartcoding.plain.platform.system.*
import com.ismartcoding.plain.ui.platform.system.*
import com.ismartcoding.plain.ui.state.*
import com.ismartcoding.plain.platform.telephony.*
import com.ismartcoding.plain.features.contacts.*
import kotlinx.serialization.json.*

internal object RustPhoneQuery {
    suspend fun contacts(query: String, limit: Int, offset: Int): List<com.ismartcoding.plain.api.graphql.models.Contact> {
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
