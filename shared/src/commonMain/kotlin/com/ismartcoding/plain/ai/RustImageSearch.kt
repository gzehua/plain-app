package com.ismartcoding.plain.ai

import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.content.gql
import com.ismartcoding.plain.data.DImage
import com.ismartcoding.plain.features.file.FileSortBy
import com.ismartcoding.plain.features.system.MediaFacts
import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.json.*
import kotlin.time.Instant

internal object RustImageSearch {
    suspend fun rows(text: String, query: String, limit: Int, offset: Int, sortBy: FileSortBy): List<DImage> {
        val response = PlainRust.mainQuery("searchImages(text: ${gql(text)}, query: ${gql(query)}, offset: $offset, limit: $limit, sortBy: ${sortBy.name}) { id title path size bucketId createdAt updatedAt durationMs takenAt isFavorite width height rotation }")
        return JsonHelper.jsonDecodeFromElement<List<MediaFacts>>(response.getValue("searchImages")).map { row ->
            DImage(row.id,row.title,row.path,row.size,row.width,row.height,row.rotation,row.bucketId,
                Instant.parse(row.createdAt),Instant.parse(row.updatedAt),row.takenAt?.let(Instant::parse),row.isFavorite)
        }
    }
    suspend fun count(text: String, query: String): Int =
        PlainRust.mainQuery("searchImageCount(text: ${gql(text)}, query: ${gql(query)})").getValue("searchImageCount").jsonPrimitive.int
}
