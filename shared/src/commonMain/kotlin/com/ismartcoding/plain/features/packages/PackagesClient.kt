package com.ismartcoding.plain.features.packages

import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.request.gql
import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.json.*
import com.ismartcoding.plain.features.files.FileSortBy
import com.ismartcoding.plain.api.graphql.models.Package as AppPackage

object PackagesClient {
    suspend fun search(query: String, limit: Int, offset: Int, sortBy: FileSortBy): List<AppPackage> =
        JsonHelper.jsonDecodeFromElement(PlainRust.mainQuery("packages(query: ${gql(query)}, offset: $offset, limit: $limit, sortBy: ${sortBy.name}) { id name type version path size installedAt updatedAt certs { issuer subject serialNumber validFrom validTo } }").getValue("packages"))
    suspend fun count(query: String): Int = PlainRust.mainQuery("packageCount(query: ${gql(query)})").getValue("packageCount").jsonPrimitive.int
}
