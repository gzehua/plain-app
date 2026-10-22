package com.ismartcoding.plain.features.packages

import com.ismartcoding.plain.rust.graphql.RustGraphqlApi

import com.ismartcoding.plain.rust.graphql.gql
import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.json.*
import com.ismartcoding.plain.features.files.FileSortBy
import com.ismartcoding.plain.models.Package as AppPackage

object PackagesClient {
    suspend fun search(query: String, limit: Int, offset: Int, sortBy: FileSortBy): List<AppPackage> =
        JsonHelper.jsonDecodeFromElement(RustGraphqlApi.query("packages(query: ${gql(query)}, offset: $offset, limit: $limit, sortBy: ${sortBy.name}) { id name type version path size installedAt updatedAt certs { issuer subject serialNumber validFrom validTo } }").getValue("packages"))
    suspend fun manifest(id: String): String = RustGraphqlApi.graphql(
        "query(\$id: ID!) { packageManifest(id: \$id) }", mapOf("id" to JsonPrimitive(id)),
    ).getValue("packageManifest").jsonPrimitive.content

    suspend fun count(query: String): Int = RustGraphqlApi.query("packageCount(query: ${gql(query)})").getValue("packageCount").jsonPrimitive.int
}
