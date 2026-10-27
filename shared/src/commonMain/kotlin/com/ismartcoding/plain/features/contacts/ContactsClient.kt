package com.ismartcoding.plain.features.contacts

import com.ismartcoding.plain.graphql.Query
import com.ismartcoding.plain.models.Contact
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi

object ContactsClient {
    suspend fun search(query: String, limit: Int, offset: Int): List<Contact> =
        RustGraphqlApi.read(Query.contacts(query, limit, offset))

    suspend fun count(query: String): Int = RustGraphqlApi.read(Query.contactCount(query))
}
