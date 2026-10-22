package com.ismartcoding.plain.features.files

import com.ismartcoding.plain.rust.graphql.RustGraphqlApi

import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

object RemoteFiles {
    suspend fun cache(url: String, name: String = ""): String = RustGraphqlApi.graphql(
        "mutation(\$url: String!, \$name: String!) { cacheRemoteFile(url: \$url, name: \$name) }",
        mapOf("url" to JsonPrimitive(url), "name" to JsonPrimitive(name)),
    ).getValue("cacheRemoteFile").jsonPrimitive.content
}
