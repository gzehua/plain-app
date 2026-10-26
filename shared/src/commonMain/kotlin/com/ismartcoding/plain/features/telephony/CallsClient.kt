package com.ismartcoding.plain.features.telephony

import com.ismartcoding.plain.graphql.Query
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi
import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.json.*
import com.ismartcoding.plain.models.Call

object CallsClient {
    suspend fun search(query: String, limit: Int, offset: Int): List<Call> =
        JsonHelper.jsonDecodeFromElement(RustGraphqlApi.graphql(Query.calls(query, limit, offset)).getValue("calls"))
    suspend fun count(query: String): Int = RustGraphqlApi.graphql(Query.callCount(query)).getValue("callCount").jsonPrimitive.int
}
