package com.ismartcoding.plain.features.media

import com.ismartcoding.plain.rust.graphql.GraphqlRequest
import com.ismartcoding.plain.graphql.Mutation
import com.ismartcoding.plain.graphql.Query
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi
import com.ismartcoding.plain.models.AudioItem
import com.ismartcoding.plain.models.ImageFileInfo
import com.ismartcoding.plain.models.VideoFileInfo
import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.json.*

object MediaClient {
    suspend fun audioInfo(path: String): AudioItem = JsonHelper.jsonDecodeFromElement(
        RustGraphqlApi.graphql(
            Query.audioMetadata(path),
        ).getValue("audioMetadata"),
    )

    suspend fun lyricsByPath(path: String): String =
        RustGraphqlApi.graphql(Query.audioLyricsByPath(path))
            .getValue("audioLyricsByPath").jsonPrimitive.contentOrNull.orEmpty()

    suspend fun cacheAudioCover(path: String): String? = RustGraphqlApi.graphql(
        Mutation.cacheAudioCover(path),
    ).getValue("cacheAudioCover").jsonPrimitive.contentOrNull

    suspend fun videoInfo(path: String): VideoFileInfo? {
        val info = RustGraphqlApi.graphql(
            Query.videoFileInfo(path),
        ).getValue("fileInfo").jsonObject.getValue("data")
        return if (info is JsonNull) null else JsonHelper.jsonDecodeFromElement(info)
    }

    suspend fun imageInfo(path: String): ImageFileInfo? {
        val info = RustGraphqlApi.graphql(
            Query.imageFileInfo(path),
        ).getValue("fileInfo").jsonObject.getValue("data")
        return if (info is JsonNull) null else JsonHelper.jsonDecodeFromElement(info)
    }

    suspend fun durationMs(path: String): Long = readMediaDurationMs(path) { request ->
        RustGraphqlApi.graphql(request)
    }
}

internal suspend fun readMediaDurationMs(
    path: String,
    query: suspend (GraphqlRequest) -> JsonObject,
): Long {
    val request = Query.audioDuration(path)
    val data = query(request).getValue("fileInfo").jsonObject.getValue("data")
    return if (data is JsonNull) 0L else data.jsonObject.getValue("durationMs").jsonPrimitive.long
}
