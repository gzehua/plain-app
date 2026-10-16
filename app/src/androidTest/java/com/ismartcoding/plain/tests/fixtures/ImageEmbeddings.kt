package com.ismartcoding.plain.tests.fixtures

import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.content.*
import com.ismartcoding.plain.graphql.models.ImageHostEmbeddingInput
import kotlin.io.encoding.Base64
import kotlinx.serialization.json.*

internal fun imageEmbedding(id: String, path: String, embedding: ByteArray): ImageHostEmbeddingInput =
    ImageHostEmbeddingInput(id, path, Base64.encode(embedding))

internal object ImageEmbeddings {
    suspend fun getAllIds(): List<String> = PlainRust.mainQuery("imageEmbeddingIds").getValue("imageEmbeddingIds").jsonArray.map { it.jsonPrimitive.content }
    suspend fun insertAll(items: List<ImageHostEmbeddingInput>) {
        val input = items.joinToString(",", "[", "]") { "{ id: ${gql(it.id)}, path: ${gql(it.path)}, embeddingBase64: ${gql(it.embeddingBase64)} }" }
        PlainRust.mainMutate("saveImageEmbeddings(items: $input)")
    }
    suspend fun deleteByIds(ids: List<String>) { PlainRust.mainMutate("removeImageEmbeddings(ids: ${gqlIds(ids)})") }
    suspend fun search(embedding: ByteArray, limit: Int): List<SemanticSearchResult> = PlainRust.mainQuery("searchImageEmbeddings(embeddingBase64: ${gql(Base64.encode(embedding))}, maxResults: $limit) { imageId score }")
        .getValue("searchImageEmbeddings").jsonArray.map { value -> value.jsonObject.let { SemanticSearchResult(it.string("imageId"), it.getValue("score").jsonPrimitive.float) } }
}
