package com.ismartcoding.plain.features.telephony.sms

import com.ismartcoding.plain.rust.graphql.RustGraphqlApi

import com.ismartcoding.plain.rust.graphql.gql
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.models.Sms
import kotlinx.serialization.json.*

object RustSmsQuery {
    private const val MESSAGE_FIELDS = "id body address sentAt serviceCenter read threadId type typeCode subscriptionId isMms attachments { path contentType name }"

    suspend fun search(query: String, limit: Int, offset: Int): List<Sms> =
        JsonHelper.jsonDecodeFromElement(RustGraphqlApi.query("sms(query: ${gql(query)}, offset: $offset, limit: $limit) { $MESSAGE_FIELDS }").getValue("sms"))

    suspend fun count(query: String): Int =
        RustGraphqlApi.query("smsCount(query: ${gql(query)})").getValue("smsCount").jsonPrimitive.int

}
