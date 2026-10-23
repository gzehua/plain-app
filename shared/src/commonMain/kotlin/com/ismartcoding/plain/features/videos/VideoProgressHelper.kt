package com.ismartcoding.plain.features.videos

import com.ismartcoding.plain.api.graphql.Mutation
import com.ismartcoding.plain.api.graphql.Query
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi
import com.ismartcoding.plain.models.VideoPlayProgress
import com.ismartcoding.plain.lib.JsonHelper
import kotlin.time.Instant
import kotlinx.serialization.json.*

object VideoProgressHelper {
    suspend fun recentAsync(since: Instant): List<VideoPlayProgress> =
        RustGraphqlApi.graphql(Query.recentVideoPlayProgress(since.toString()))
            .getValue("recentVideoPlayProgress").jsonArray.map { it.progress() }

    suspend fun saveAsync(mediaId: String, positionMs: Long): VideoPlayProgress =
        RustGraphqlApi.graphql(Mutation.saveVideoPlayProgress(mediaId, positionMs))
            .getValue("saveVideoPlayProgress").progress()

    suspend fun recordPlayAsync(mediaId: String, positionMs: Long) {
        RustGraphqlApi.graphql(Mutation.recordVideoPlay(mediaId, positionMs))
    }

    private fun JsonElement.progress(): VideoPlayProgress = JsonHelper.jsonDecodeFromElement<VideoPlayProgress>(this)
}
