package com.ismartcoding.plain.features.chat

import com.ismartcoding.plain.rust.graphql.GraphqlRequest
import com.ismartcoding.plain.graphql.Query
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi
import com.ismartcoding.plain.models.ChatItem
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject

internal suspend fun readChatSnapshot(
    ids: List<String>,
    fetch: suspend (GraphqlRequest) -> JsonObject = { RustGraphqlApi.graphql(it) },
): Map<String, ChatItem?> {
    val result = linkedMapOf<String, ChatItem?>()
    for (batch in ids.distinct().chunked(128)) {
        val selection = Query.chatSnapshot(batch)
        val data = fetch(selection)
        batch.forEachIndexed { index, id ->
            val value = data.getValue("message$index")
            result[id] = if (value is JsonNull) null else RustChatStore.decodeGraphql(value).also {
                require(it.id == id) { "Mismatched chat snapshot identity" }
            }
        }
    }
    return result
}
