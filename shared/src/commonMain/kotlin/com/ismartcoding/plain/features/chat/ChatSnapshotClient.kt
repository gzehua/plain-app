package com.ismartcoding.plain.features.chat

import com.ismartcoding.plain.graphql.Query
import com.ismartcoding.plain.models.ChatItem
import com.ismartcoding.plain.rust.graphql.GraphqlRequest
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi
import kotlinx.serialization.json.JsonObject

internal suspend fun readChatSnapshot(
    ids: List<String>,
    fetch: suspend (GraphqlRequest) -> JsonObject = { RustGraphqlApi.graphql(it) },
): Map<String, ChatItem?> {
    val result = linkedMapOf<String, ChatItem?>()
    for (batch in ids.distinct().chunked(128)) {
        val selection = Query.chatSnapshot(batch)
        val data = RustGraphqlApi.readResponse<Map<String, ChatItem?>>(selection, fetch)
        batch.forEachIndexed { index, id ->
            result[id] = data.getValue("message$index")?.also {
                require(it.id == id) { "Mismatched chat snapshot identity" }
            }
        }
    }
    return result
}
