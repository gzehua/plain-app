package com.ismartcoding.plain.ui.platform.media

import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import androidx.compose.ui.graphics.ImageBitmap
import com.ismartcoding.plain.api.graphql.models.AudioItem

suspend fun getAudioMetadata(path: String): Pair<String, String> =
    playlistAudioFromPath(path).let { it.title to it.artist }

/** Lyrics embedded in the audio file metadata, empty when absent. */
suspend fun getAudioLyrics(path: String): String =
    com.ismartcoding.plain.rust.PlainRust.mainQuery("embeddedAudioLyrics(path: ${com.ismartcoding.plain.rust.request.gql(path)})")
        .getValue("embeddedAudioLyrics").jsonPrimitive.contentOrNull.orEmpty()

suspend fun playlistAudioFromPath(path: String): AudioItem =
    com.ismartcoding.plain.lib.JsonHelper.jsonDecodeFromElement(
        com.ismartcoding.plain.rust.PlainRust.mainGraphql(
            "query AudioMetadata(\$path: String!) { audioMetadata(path: \$path) { title artist path durationMs } }",
            mapOf("path" to kotlinx.serialization.json.JsonPrimitive(path)),
        ).getValue("audioMetadata"),
    )

suspend fun loadAudioCoverBitmap(path: String): ImageBitmap? = try {
    val cached = com.ismartcoding.plain.rust.PlainRust.mainGraphql(
        "mutation CacheAudioCover(\$path: String!) { cacheAudioCover(path: \$path) }",
        mapOf("path" to kotlinx.serialization.json.JsonPrimitive(path)),
    ).getValue("cacheAudioCover").jsonPrimitive.contentOrNull
    cached?.let(::decodeImageFileBitmap)
} catch (error: kotlinx.coroutines.CancellationException) {
    throw error
} catch (error: Exception) {
    com.ismartcoding.plain.lib.logcat.LogCat.e("Audio cover load failed: ${error.message}")
    null
}

expect fun audioAlbumArtUri(albumId: String): String
