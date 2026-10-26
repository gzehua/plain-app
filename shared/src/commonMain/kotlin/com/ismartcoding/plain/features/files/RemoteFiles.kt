package com.ismartcoding.plain.features.files

import com.ismartcoding.plain.graphql.Mutation
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi
import kotlinx.serialization.json.jsonPrimitive

object RemoteFiles {
    suspend fun cache(url: String, name: String = ""): String = RustGraphqlApi.graphql(
        Mutation.cacheRemoteFile(url, name),
    ).getValue("cacheRemoteFile").jsonPrimitive.content
}
