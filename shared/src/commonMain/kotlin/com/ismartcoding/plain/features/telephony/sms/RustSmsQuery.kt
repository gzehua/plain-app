package com.ismartcoding.plain.features.telephony.sms

import com.ismartcoding.plain.graphql.Query
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.models.Sms
import kotlinx.serialization.json.*

object RustSmsQuery {
    suspend fun search(query: String, limit: Int, offset: Int): List<Sms> =
        JsonHelper.jsonDecodeFromElement(RustGraphqlApi.graphql(Query.sms(query, offset, limit)).getValue("sms"))

    suspend fun count(query: String): Int =
        RustGraphqlApi.graphql(Query.smsCount(query)).getValue("smsCount").jsonPrimitive.int
}
