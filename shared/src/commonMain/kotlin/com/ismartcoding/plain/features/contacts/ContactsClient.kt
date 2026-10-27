package com.ismartcoding.plain.features.contacts

import com.ismartcoding.plain.graphql.Query
import com.ismartcoding.plain.models.Contact
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi

object ContactsClient {
    suspend fun search(query: String, limit: Int, offset: Int): List<Contact> =
        RustGraphqlApi.execute(Query.contacts(query, limit, offset))

    suspend fun count(query: String): Int = RustGraphqlApi.execute(Query.contactCount(query))
}
