package com.ismartcoding.plain.features

import com.ismartcoding.plain.graphql.models.VideoPlayProgress
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.rust.bridge.*
import com.ismartcoding.plain.rust.content.*
import com.ismartcoding.plain.rust.event.*
import com.ismartcoding.plain.rust.host.*
import com.ismartcoding.plain.rust.http.*
import com.ismartcoding.plain.rust.request.*
import kotlin.time.Instant
import kotlinx.serialization.json.*

object VideoProgressHelper {
    private const val FIELDS = "mediaId positionMs updatedAt"

    suspend fun recentAsync(since: Instant): List<VideoPlayProgress> =
        RustContentApi.query("recentVideoPlayProgress(since: ${gql(since.toString())}) { $FIELDS }")
            .getValue("recentVideoPlayProgress").jsonArray.map { it.progress() }

    suspend fun getAsync(mediaId: String): VideoPlayProgress? =
        RustContentApi.query("videoPlayProgress(mediaId: ${gql(mediaId)}) { $FIELDS }")
            .getValue("videoPlayProgress").takeUnless { it is JsonNull }?.progress()

    suspend fun saveAsync(mediaId: String, positionMs: Long): VideoPlayProgress =
        RustContentApi.mutate("saveVideoPlayProgress(mediaId: ${gql(mediaId)}, positionMs: $positionMs) { $FIELDS }")
            .getValue("saveVideoPlayProgress").progress()

    suspend fun deleteAsync(mediaId: String) {
        RustContentApi.mutate("deleteVideoPlayProgress(mediaId: ${gql(mediaId)})")
    }

    private fun JsonElement.progress(): VideoPlayProgress = JsonHelper.jsonDecodeFromElement<VideoPlayProgress>(this)
}
