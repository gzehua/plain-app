package com.ismartcoding.plain.features.packages

import com.ismartcoding.plain.graphql.Query
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi
import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.json.*
import com.ismartcoding.plain.features.files.FileSortBy
import com.ismartcoding.plain.models.Package as AppPackage

object PackagesClient {
    suspend fun search(query: String, limit: Int, offset: Int, sortBy: FileSortBy): List<AppPackage> =
        JsonHelper.jsonDecodeFromElement(RustGraphqlApi.graphql(Query.packages(query, offset, limit, sortBy.name)).getValue("packages"))
    suspend fun manifest(id: String): String = RustGraphqlApi.graphql(
        Query.packageManifest(id),
    ).getValue("packageManifest").jsonPrimitive.content

    suspend fun count(query: String): Int = RustGraphqlApi.graphql(Query.packageCount(query)).getValue("packageCount").jsonPrimitive.int
}
