package com.ismartcoding.plain.features.packages

import com.ismartcoding.plain.features.files.FileSortBy
import com.ismartcoding.plain.graphql.Query
import com.ismartcoding.plain.models.Package as AppPackage
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi

object PackagesClient {
    suspend fun search(query: String, limit: Int, offset: Int, sortBy: FileSortBy): List<AppPackage> =
        RustGraphqlApi.execute(Query.packages(query, offset, limit, sortBy))

    suspend fun manifest(id: String): String = RustGraphqlApi.execute(Query.packageManifest(id))

    suspend fun count(query: String): Int = RustGraphqlApi.execute(Query.packageCount(query))
}
