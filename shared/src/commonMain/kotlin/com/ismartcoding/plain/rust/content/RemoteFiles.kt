package com.ismartcoding.plain.rust.content

import com.ismartcoding.plain.rust.PlainRust
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

object RemoteFiles {
    suspend fun cache(url: String, name: String = ""): String = PlainRust.mainGraphql(
        "mutation(\$url: String!, \$name: String!) { cacheRemoteFile(url: \$url, name: \$name) }",
        mapOf("url" to JsonPrimitive(url), "name" to JsonPrimitive(name)), longRunning = true,
    ).getValue("cacheRemoteFile").jsonPrimitive.content
}
