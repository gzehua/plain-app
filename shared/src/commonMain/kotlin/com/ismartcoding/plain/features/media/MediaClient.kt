package com.ismartcoding.plain.features.media

import com.ismartcoding.plain.graphql.Mutation
import com.ismartcoding.plain.graphql.Query
import com.ismartcoding.plain.models.AudioItem
import com.ismartcoding.plain.models.ImageFileInfo
import com.ismartcoding.plain.models.VideoFileInfo
import com.ismartcoding.plain.rust.graphql.GraphqlRequest
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

object MediaClient {
    suspend fun audioInfo(path: String): AudioItem =
        RustGraphqlApi.read(Query.audioMetadata(path))

    suspend fun lyricsByPath(path: String): String =
        RustGraphqlApi.read<String?>(Query.audioLyricsByPath(path)).orEmpty()

    suspend fun cacheAudioCover(path: String): String? =
        RustGraphqlApi.read(Mutation.cacheAudioCover(path))

    suspend fun videoInfo(path: String): VideoFileInfo? =
        RustGraphqlApi.read<MediaFileInfo<VideoFileInfo>>(Query.videoFileInfo(path)).data

    suspend fun imageInfo(path: String): ImageFileInfo? =
        RustGraphqlApi.read<MediaFileInfo<ImageFileInfo>>(Query.imageFileInfo(path)).data

    suspend fun durationMs(path: String): Long = readMediaDurationMs(path) { request ->
        RustGraphqlApi.graphql(request)
    }
}
internal suspend fun readMediaDurationMs(
    path: String,
    query: suspend (GraphqlRequest) -> JsonObject,
): Long {
    val info = RustGraphqlApi.read<MediaFileInfo<AudioDuration>>(Query.audioDuration(path), query)
    return info.data?.durationMs ?: 0L
}

@Serializable
private data class MediaFileInfo<T>(val data: T?)

@Serializable
private data class AudioDuration(val durationMs: Long)
