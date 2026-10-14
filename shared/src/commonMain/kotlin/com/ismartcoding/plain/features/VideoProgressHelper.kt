package com.ismartcoding.plain.features

import com.ismartcoding.plain.graphql.models.VideoPlayProgress
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.bridge.*
import com.ismartcoding.plain.rust.content.*
import com.ismartcoding.plain.rust.event.*
import com.ismartcoding.plain.rust.host.*
import com.ismartcoding.plain.rust.http.*
import kotlin.time.Instant
import kotlinx.serialization.json.*

object VideoProgressHelper {
    private const val FIELDS = "mediaId positionMs updatedAt"

    suspend fun recentAsync(since: Instant): List<VideoPlayProgress> =
        PlainRust.privateQuery("recentVideoPlayProgress(since: ${gql(since.toString())}) { $FIELDS }")
            .getValue("recentVideoPlayProgress").jsonArray.map { it.progress() }

    suspend fun getAsync(mediaId: String): VideoPlayProgress? =
        PlainRust.privateQuery("videoPlayProgress(mediaId: ${gql(mediaId)}) { $FIELDS }")
            .getValue("videoPlayProgress").takeUnless { it is JsonNull }?.progress()

    suspend fun saveAsync(mediaId: String, positionMs: Long): VideoPlayProgress =
        PlainRust.privateMutate("saveVideoPlayProgress(mediaId: ${gql(mediaId)}, positionMs: $positionMs) { $FIELDS }")
            .getValue("saveVideoPlayProgress").progress()

    suspend fun recordPlayAsync(mediaId: String, positionMs: Long) {
        PlainRust.privateMutate("recordVideoPlay(mediaId: ${gql(mediaId)}, positionMs: $positionMs) { mediaId }")
    }

    suspend fun deleteAsync(mediaId: String) {
        PlainRust.privateMutate("deleteVideoPlayProgress(mediaId: ${gql(mediaId)})")
    }

    private fun JsonElement.progress(): VideoPlayProgress = JsonHelper.jsonDecodeFromElement<VideoPlayProgress>(this)
}
