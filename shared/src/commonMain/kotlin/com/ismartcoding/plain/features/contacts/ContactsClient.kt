package com.ismartcoding.plain.features.contacts

import com.ismartcoding.plain.rust.graphql.RustGraphqlApi

import com.ismartcoding.plain.rust.graphql.gql
import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.json.*

object ContactsClient {
    suspend fun search(query: String, limit: Int, offset: Int): List<com.ismartcoding.plain.models.Contact> {
        val fields = "id prefix firstName middleName lastName suffix nickname photoId phoneNumbers { value type typeCode label normalizedNumber } emails { value type typeCode label } addresses { value type typeCode label } events { value type typeCode label } websites { value type typeCode label } ims { value protocol customProtocol typeCode } source starred contactId thumbnailId notes groups { id name } organization { company title } ringtone updatedAt"
        return JsonHelper.jsonDecodeFromElement(RustGraphqlApi.query("contacts(query: ${gql(query)}, limit: $limit, offset: $offset) { $fields }").getValue("contacts"))
    }
    suspend fun count(query: String): Int = RustGraphqlApi.query("contactCount(query: ${gql(query)})").getValue("contactCount").jsonPrimitive.int
}
