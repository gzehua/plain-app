package com.ismartcoding.plain.features.telephony

import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.request.gql
import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.json.*

object CallsClient {
    suspend fun search(query: String, limit: Int, offset: Int): List<com.ismartcoding.plain.api.graphql.models.Call> =
        JsonHelper.jsonDecodeFromElement(PlainRust.mainQuery("calls(query: ${gql(query)}, limit: $limit, offset: $offset) { id number name photoId photoUri startedAt durationSec type typeCode accountId }").getValue("calls"))
    suspend fun count(query: String): Int = PlainRust.mainQuery("callCount(query: ${gql(query)})").getValue("callCount").jsonPrimitive.int
}
