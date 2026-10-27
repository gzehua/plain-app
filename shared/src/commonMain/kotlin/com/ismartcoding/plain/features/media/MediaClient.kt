package com.ismartcoding.plain.features.media

import com.ismartcoding.plain.graphql.Mutation
import com.ismartcoding.plain.graphql.Query
import com.ismartcoding.plain.models.AudioItem
import com.ismartcoding.plain.models.ImageFileInfo
import com.ismartcoding.plain.models.VideoFileInfo
import com.ismartcoding.plain.rust.graphql.GraphqlClient
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi

object MediaClient {
    suspend fun audioInfo(path: String): AudioItem =
        RustGraphqlApi.execute(Query.audioMetadata(path))

    suspend fun lyricsByPath(path: String): String =
        RustGraphqlApi.execute(Query.audioLyricsByPath(path)).orEmpty()

    suspend fun cacheAudioCover(path: String): String? =
        RustGraphqlApi.execute(Mutation.cacheAudioCover(path))

    suspend fun videoInfo(path: String): VideoFileInfo? =
        RustGraphqlApi.execute(Query.videoFileInfo(path)).data

    suspend fun imageInfo(path: String): ImageFileInfo? =
        RustGraphqlApi.execute(Query.imageFileInfo(path)).data

    suspend fun durationMs(path: String): Long = readMediaDurationMs(path)
}
internal suspend fun readMediaDurationMs(
    path: String,
    api: GraphqlClient = RustGraphqlApi,
): Long {
    val info = api.execute(Query.audioDuration(path))
    return info.data?.durationMs ?: 0L
}
