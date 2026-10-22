package com.ismartcoding.plain.features.chat

import com.ismartcoding.plain.models.ChatItem
import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.request.gql
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject

internal suspend fun readChatSnapshot(
    ids: List<String>,
    fetch: suspend (String) -> JsonObject = { PlainRust.mainQuery(it) },
): Map<String, ChatItem?> {
    val result = linkedMapOf<String, ChatItem?>()
    for (batch in ids.distinct().chunked(128)) {
        val selection = batch.mapIndexed { index, id ->
            "message$index:chatItem(id:${gql(id)}){${RustChatStore.FIELDS}}"
        }.joinToString(" ")
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
