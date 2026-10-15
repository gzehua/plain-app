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
    suspend fun getAllIds(): List<String> = PlainRust.mainQuery("imageEmbeddingIds").getValue("imageEmbeddingIds").jsonArray.map { it.jsonPrimitive.content }
    suspend fun count(): Long = PlainRust.mainQuery("imageEmbeddingCount").getValue("imageEmbeddingCount").jsonPrimitive.long
    suspend fun insertAll(items: List<DImageEmbedding>) {
        val input = items.joinToString(",", "[", "]") { "{ id: ${gql(it.id)}, path: ${gql(it.path)}, embeddingBase64: ${gql(Base64.encode(it.embedding))} }" }
        PlainRust.mainMutate("saveImageEmbeddings(items: $input)")
    }
    suspend fun deleteByIds(ids: List<String>) { PlainRust.mainMutate("removeImageEmbeddings(ids: ${gqlIds(ids)})") }
    suspend fun deleteAll() { PlainRust.mainMutate("clearImageEmbeddings") }
    suspend fun search(embedding: ByteArray, limit: Int): List<SemanticSearchResult> = PlainRust.mainQuery("searchImageEmbeddings(embeddingBase64: ${gql(Base64.encode(embedding))}, maxResults: $limit) { imageId score }")
        .getValue("searchImageEmbeddings").jsonArray.map { value -> value.jsonObject.let { SemanticSearchResult(it.string("imageId"), it.getValue("score").jsonPrimitive.float) } }
}
