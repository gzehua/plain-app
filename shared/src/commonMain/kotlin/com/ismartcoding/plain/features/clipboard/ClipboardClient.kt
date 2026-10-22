package com.ismartcoding.plain.features.clipboard

import com.ismartcoding.plain.rust.graphql.RustGraphqlApi

import com.ismartcoding.plain.features.chat.peer.RustPeerStore
import com.ismartcoding.plain.models.ClipboardItem
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.rust.graphql.gql
import com.ismartcoding.plain.rust.graphql.selectionQuery
import kotlinx.serialization.json.*

object ClipboardClient {
    private const val FIELDS = "id text source label sensitive createdAt"

    suspend fun getPage(limit: Int, offset: Int, query: String = ""): List<ClipboardItem> =
        RustGraphqlApi.query("clipboardItems(limit: $limit, offset: $offset, query: ${gql(query)}) { $FIELDS }")
            .getValue("clipboardItems").jsonArray.map { it.clipboard() }

    suspend fun count(query: String = ""): Int = RustGraphqlApi.query("clipboardItemCount(query: ${gql(query)})")
        .getValue("clipboardItemCount").jsonPrimitive.int

    suspend fun deleteByIds(ids: List<String>): Int {
        if (ids.isEmpty()) return 0
        return delete(selectionQuery(ids.toSet()))
    }

    suspend fun delete(query: String): Int = RustGraphqlApi.mutate("deleteClipboardItems(query: ${gql(query)}) { affectedCount }")
        .getValue("deleteClipboardItems").jsonObject.getValue("affectedCount").jsonPrimitive.int

    suspend fun clear() { delete("all:true") }

    suspend fun getSourceName(source: String): String =
        if (source.isBlank()) "" else RustPeerStore.getById(source)?.name ?: source


    private fun JsonElement.clipboard(): ClipboardItem = JsonHelper.jsonDecodeFromElement<ClipboardItem>(this)
}
