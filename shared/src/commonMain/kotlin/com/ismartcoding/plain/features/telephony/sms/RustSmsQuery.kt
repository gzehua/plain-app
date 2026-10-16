package com.ismartcoding.plain.features.telephony.sms

import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.request.gql
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.api.graphql.models.Sms
import kotlinx.serialization.json.*

object RustSmsQuery {
    private const val MESSAGE_FIELDS = "id body address sentAt serviceCenter read threadId type typeCode subscriptionId isMms attachments { path contentType name }"

    suspend fun search(query: String, limit: Int, offset: Int, includeTrashed: Boolean = false): List<Sms> =
        JsonHelper.jsonDecodeFromElement(PlainRust.mainQuery("sms(query: ${gql(query)}, offset: $offset, limit: $limit, includeTrashed: $includeTrashed) { $MESSAGE_FIELDS }").getValue("sms"))

    suspend fun ids(query: String, includeTrashed: Boolean = false): Set<String> =
        PlainRust.mainQuery("smsIds(query: ${gql(query)}, includeTrashed: $includeTrashed)")
            .getValue("smsIds").jsonArray.map { it.jsonPrimitive.content }.toSet()

    suspend fun textIds(filters: List<String>): Set<String> =
        PlainRust.mainQuery("mmsTextIds(filters: [${filters.joinToString(",", transform = ::gql)}])")
            .getValue("mmsTextIds").jsonArray.map { it.jsonPrimitive.content }.toSet()

    suspend fun count(query: String): Int =
        PlainRust.mainQuery("smsCount(query: ${gql(query)})").getValue("smsCount").jsonPrimitive.int

}
