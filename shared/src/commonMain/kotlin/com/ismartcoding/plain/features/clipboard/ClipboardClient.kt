package com.ismartcoding.plain.features.clipboard

import com.ismartcoding.plain.graphql.Mutation
import com.ismartcoding.plain.graphql.Query
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi
import com.ismartcoding.plain.features.chat.peer.RustPeerStore
import com.ismartcoding.plain.models.ClipboardItem
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.rust.graphql.selectionQuery
import kotlinx.serialization.json.*

object ClipboardClient {
    suspend fun getPage(limit: Int, offset: Int, query: String = ""): List<ClipboardItem> =
        RustGraphqlApi.graphql(Query.clipboardItems(limit, offset, query))
            .getValue("clipboardItems").jsonArray.map { it.clipboard() }

    suspend fun count(query: String = ""): Int = RustGraphqlApi.graphql(Query.clipboardItemCount(query))
        .getValue("clipboardItemCount").jsonPrimitive.int

    suspend fun deleteByIds(ids: List<String>): Int {
        if (ids.isEmpty()) return 0
        return delete(selectionQuery(ids.toSet()))
    }

    suspend fun delete(query: String): Int = RustGraphqlApi.graphql(Mutation.deleteClipboardItems(query))
        .getValue("deleteClipboardItems").jsonObject.getValue("affectedCount").jsonPrimitive.int

    suspend fun clear() { delete("all:true") }

    suspend fun getSourceName(source: String): String =
        if (source.isBlank()) "" else RustPeerStore.getById(source)?.name ?: source

    private fun JsonElement.clipboard(): ClipboardItem = JsonHelper.jsonDecodeFromElement<ClipboardItem>(this)
}
