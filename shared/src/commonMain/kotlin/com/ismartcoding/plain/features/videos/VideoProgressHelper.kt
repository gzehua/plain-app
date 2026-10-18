package com.ismartcoding.plain.features.videos

import com.ismartcoding.plain.api.graphql.models.VideoPlayProgress
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.request.gql
import kotlin.time.Instant
import kotlinx.serialization.json.*

object VideoProgressHelper {
    private const val FIELDS = "mediaId positionMs updatedAt"

    suspend fun recentAsync(since: Instant): List<VideoPlayProgress> =
        PlainRust.mainQuery("recentVideoPlayProgress(since: ${gql(since.toString())}) { $FIELDS }")
            .getValue("recentVideoPlayProgress").jsonArray.map { it.progress() }

    suspend fun saveAsync(mediaId: String, positionMs: Long): VideoPlayProgress =
        PlainRust.mainMutate("saveVideoPlayProgress(mediaId: ${gql(mediaId)}, positionMs: $positionMs) { $FIELDS }")
            .getValue("saveVideoPlayProgress").progress()

    suspend fun recordPlayAsync(mediaId: String, positionMs: Long) {
        PlainRust.mainMutate("recordVideoPlay(mediaId: ${gql(mediaId)}, positionMs: $positionMs) { mediaId }")
    }

    private fun JsonElement.progress(): VideoPlayProgress = JsonHelper.jsonDecodeFromElement<VideoPlayProgress>(this)
}
