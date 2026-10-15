package com.ismartcoding.plain.features

import com.ismartcoding.plain.chat.peer.RustPeerStore
import com.ismartcoding.plain.graphql.models.ClipboardItem
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.content.gql
import com.ismartcoding.plain.rust.content.gqlIds
import kotlinx.serialization.json.*

object ClipboardHelper {
    private const val FIELDS = "id text source label sensitive createdAt"

    suspend fun getPage(limit: Int, offset: Int, query: String = ""): List<ClipboardItem> =
        PlainRust.mainQuery("clipboardItems(limit: $limit, offset: $offset, query: ${gql(query)}) { $FIELDS }")
            .getValue("clipboardItems").jsonArray.map { it.clipboard() }

    suspend fun count(query: String = ""): Int = PlainRust.mainQuery("clipboardItemCount(query: ${gql(query)})")
        .getValue("clipboardItemCount").jsonPrimitive.int

    suspend fun record(text: String, source: String = "", label: String = "", sensitive: Boolean = false): ClipboardItem? {
        val input = "{ text: ${gql(text)}, source: ${gql(source)}, label: ${gql(label)}, sensitive: $sensitive }"
        val result = PlainRust.mainMutate("recordClipboard(input: $input) { inserted item { $FIELDS } }")
            .getValue("recordClipboard").jsonObject
        return if (result.getValue("inserted").jsonPrimitive.boolean) result.getValue("item").clipboard() else null
    }

    suspend fun deleteByIds(ids: List<String>): Int {
        if (ids.isEmpty()) return 0
        return PlainRust.mainMutate("deleteClipboardItemsByIds(ids: ${gqlIds(ids)}) { affectedCount }")
            .getValue("deleteClipboardItemsByIds").jsonObject.getValue("affectedCount").jsonPrimitive.int
    }

    suspend fun delete(query: String): Int = PlainRust.mainMutate("deleteClipboardItems(query: ${gql(query)}) { affectedCount }")
        .getValue("deleteClipboardItems").jsonObject.getValue("affectedCount").jsonPrimitive.int

    suspend fun clear() { delete("all:true") }

    suspend fun getSourceName(source: String): String =
        if (source.isBlank()) "" else RustPeerStore.getById(source)?.name ?: source


    private fun JsonElement.clipboard(): ClipboardItem = JsonHelper.jsonDecodeFromElement<ClipboardItem>(this)
}
