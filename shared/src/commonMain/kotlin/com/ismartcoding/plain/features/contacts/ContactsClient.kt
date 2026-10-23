package com.ismartcoding.plain.features.contacts

import com.ismartcoding.plain.api.graphql.Query
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi
import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.json.*

object ContactsClient {
    suspend fun search(query: String, limit: Int, offset: Int): List<com.ismartcoding.plain.models.Contact> {

        return JsonHelper.jsonDecodeFromElement(RustGraphqlApi.graphql(Query.contacts(query, limit, offset)).getValue("contacts"))
    }
    suspend fun count(query: String): Int = RustGraphqlApi.graphql(Query.contactCount(query)).getValue("contactCount").jsonPrimitive.int
}
