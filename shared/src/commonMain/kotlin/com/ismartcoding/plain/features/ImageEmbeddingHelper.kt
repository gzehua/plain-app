package com.ismartcoding.plain.features

import com.ismartcoding.plain.ai.SemanticSearchResult
import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.event.*
import com.ismartcoding.plain.rust.host.*
import com.ismartcoding.plain.rust.bridge.*
import com.ismartcoding.plain.rust.http.*
import com.ismartcoding.plain.rust.content.*
import com.ismartcoding.plain.db.DImageEmbedding
import kotlin.io.encoding.Base64
import kotlinx.serialization.json.*

object ImageEmbeddingHelper {
    suspend fun getAllIds(): List<String> = PlainRust.privateQuery("imageHostEmbeddingIds").getValue("imageHostEmbeddingIds").jsonArray.map { it.jsonPrimitive.content }
    suspend fun count(): Long = PlainRust.privateQuery("imageHostEmbeddingCount").getValue("imageHostEmbeddingCount").jsonPrimitive.long
    suspend fun insertAll(items: List<DImageEmbedding>) {
        val input = items.joinToString(",", "[", "]") { "{ id: ${gql(it.id)}, path: ${gql(it.path)}, embeddingBase64: ${gql(Base64.encode(it.embedding))} }" }
        PlainRust.privateMutate("imageHostSaveEmbeddings(items: $input)")
    }
    suspend fun deleteByIds(ids: List<String>) { PlainRust.privateMutate("imageHostRemoveEmbeddings(ids: ${gqlIds(ids)})") }
    suspend fun deleteAll() { PlainRust.privateMutate("imageHostClearEmbeddings") }
    suspend fun search(embedding: ByteArray, limit: Int): List<SemanticSearchResult> = PlainRust.privateQuery("imageHostSearch(embeddingBase64: ${gql(Base64.encode(embedding))}, maxResults: $limit) { imageId score }")
        .getValue("imageHostSearch").jsonArray.map { value -> value.jsonObject.let { SemanticSearchResult(it.string("imageId"), it.getValue("score").jsonPrimitive.float) } }
}
