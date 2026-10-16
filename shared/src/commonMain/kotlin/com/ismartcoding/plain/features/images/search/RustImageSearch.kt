package com.ismartcoding.plain.features.images.search


import com.ismartcoding.plain.api.graphql.models.Image as MediaImage
import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.request.gql
import com.ismartcoding.plain.features.files.FileSortBy
import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.json.*

internal object RustImageSearch {
    suspend fun rows(text: String, query: String, limit: Int, offset: Int, sortBy: FileSortBy): List<MediaImage> {
        return JsonHelper.jsonDecodeFromElement(PlainRust.mainQuery("searchImages(text: ${gql(text)}, query: ${gql(query)}, offset: $offset, limit: $limit, sortBy: ${sortBy.name}) { id title path size bucketId createdAt updatedAt durationMs takenAt isFavorite width height rotation }").getValue("searchImages"))
    }
    suspend fun count(text: String, query: String): Int =
        PlainRust.mainQuery("searchImageCount(text: ${gql(text)}, query: ${gql(query)})").getValue("searchImageCount").jsonPrimitive.int
}
