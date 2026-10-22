package com.ismartcoding.plain.features.telephony.sms

import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.request.gql
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.models.Sms
import kotlinx.serialization.json.*

object RustSmsQuery {
    private const val MESSAGE_FIELDS = "id body address sentAt serviceCenter read threadId type typeCode subscriptionId isMms attachments { path contentType name }"

    suspend fun search(query: String, limit: Int, offset: Int): List<Sms> =
        JsonHelper.jsonDecodeFromElement(PlainRust.mainQuery("sms(query: ${gql(query)}, offset: $offset, limit: $limit) { $MESSAGE_FIELDS }").getValue("sms"))

    suspend fun count(query: String): Int =
        PlainRust.mainQuery("smsCount(query: ${gql(query)})").getValue("smsCount").jsonPrimitive.int

}
