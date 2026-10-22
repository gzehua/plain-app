package com.ismartcoding.plain.features.media

import com.ismartcoding.plain.rust.graphql.RustGraphqlApi

import com.ismartcoding.plain.models.AudioItem
import com.ismartcoding.plain.models.ImageFileInfo
import com.ismartcoding.plain.models.VideoFileInfo
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.rust.graphql.gql
import kotlinx.serialization.json.*

object MediaClient {
    suspend fun audioInfo(path: String): AudioItem = JsonHelper.jsonDecodeFromElement(
        RustGraphqlApi.graphql(
            "query AudioMetadata(\$path: String!) { audioMetadata(path: \$path) { title artist path durationMs } }",
            mapOf("path" to JsonPrimitive(path)),
        ).getValue("audioMetadata"),
    )

    suspend fun lyricsByPath(path: String): String =
        RustGraphqlApi.query("audioLyricsByPath(path: ${gql(path)})")
            .getValue("audioLyricsByPath").jsonPrimitive.contentOrNull.orEmpty()

    suspend fun cacheAudioCover(path: String): String? = RustGraphqlApi.graphql(
        "mutation CacheAudioCover(\$path: String!) { cacheAudioCover(path: \$path) }",
        mapOf("path" to JsonPrimitive(path)),
    ).getValue("cacheAudioCover").jsonPrimitive.contentOrNull

    suspend fun videoInfo(path: String): VideoFileInfo? {
        val info = RustGraphqlApi.graphql(
            "query(\$path:String!,\$name:String!){fileInfo(path:\$path,fileName:\$name){data{__typename ... on VideoFileInfo{width height rotation displayWidth displayHeight durationMs bitrate frameRate title artist album genre takenAt writer composer location{latitude longitude}}}}}",
            mapOf("path" to JsonPrimitive(path), "name" to JsonPrimitive("video.mp4")),
        ).getValue("fileInfo").jsonObject.getValue("data")
        return if (info is JsonNull) null else JsonHelper.jsonDecodeFromElement(info)
    }

    suspend fun imageInfo(path: String): ImageFileInfo? {
        val info = RustGraphqlApi.graphql(
            "query(\$path:String!,\$name:String!){fileInfo(path:\$path,fileName:\$name){data{__typename ... on ImageFileInfo{width height rotation make model colorSpace apertureValue exposureTime focalLength isoSpeed takenAt flash fNumber exposureProgram meteringMode whiteBalance creator resolutionX resolutionY description location{latitude longitude}}}}}",
            mapOf("path" to JsonPrimitive(path), "name" to JsonPrimitive("image.jpg")),
        ).getValue("fileInfo").jsonObject.getValue("data")
        return if (info is JsonNull) null else JsonHelper.jsonDecodeFromElement(info)
    }

    suspend fun durationMs(path: String): Long = readMediaDurationMs(path) { document, variables ->
        RustGraphqlApi.graphql(document, variables)
    }
}

internal suspend fun readMediaDurationMs(
    path: String,
    query: suspend (String, Map<String, JsonElement>) -> JsonObject,
): Long {
    val data = query(
        "query(\$path:String!,\$name:String!){fileInfo(path:\$path,fileName:\$name){data{... on AudioFileInfo{durationMs}}}}",
        mapOf("path" to JsonPrimitive(path), "name" to JsonPrimitive("audio.mp3")),
    ).getValue("fileInfo").jsonObject.getValue("data")
    return if (data is JsonNull) 0L else data.jsonObject.getValue("durationMs").jsonPrimitive.long
}
