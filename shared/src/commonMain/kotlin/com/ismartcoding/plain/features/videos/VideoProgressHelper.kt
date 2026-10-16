package com.ismartcoding.plain.features.videos

import com.ismartcoding.plain.api.graphql.models.VideoPlayProgress
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.bridge.*
import com.ismartcoding.plain.features.feeds.*
import com.ismartcoding.plain.ui.feeds.*
import com.ismartcoding.plain.features.files.*
import com.ismartcoding.plain.ui.files.*
import com.ismartcoding.plain.features.notes.*
import com.ismartcoding.plain.ui.notes.*
import com.ismartcoding.plain.features.tags.*
import com.ismartcoding.plain.ui.tags.*
import com.ismartcoding.plain.rust.request.*
import com.ismartcoding.plain.rust.event.*
import com.ismartcoding.plain.rust.host.*
import com.ismartcoding.plain.rust.http.*
import kotlin.time.Instant
import kotlinx.serialization.json.*

object VideoProgressHelper {
    private const val FIELDS = "mediaId positionMs updatedAt"

    suspend fun recentAsync(since: Instant): List<VideoPlayProgress> =
        PlainRust.mainQuery("recentVideoPlayProgress(since: ${gql(since.toString())}) { $FIELDS }")
            .getValue("recentVideoPlayProgress").jsonArray.map { it.progress() }

    suspend fun getAsync(mediaId: String): VideoPlayProgress? =
        PlainRust.mainQuery("videoPlayProgress(mediaId: ${gql(mediaId)}) { $FIELDS }")
            .getValue("videoPlayProgress").takeUnless { it is JsonNull }?.progress()

    suspend fun saveAsync(mediaId: String, positionMs: Long): VideoPlayProgress =
        PlainRust.mainMutate("saveVideoPlayProgress(mediaId: ${gql(mediaId)}, positionMs: $positionMs) { $FIELDS }")
            .getValue("saveVideoPlayProgress").progress()

    suspend fun recordPlayAsync(mediaId: String, positionMs: Long) {
        PlainRust.mainMutate("recordVideoPlay(mediaId: ${gql(mediaId)}, positionMs: $positionMs) { mediaId }")
    }

    suspend fun deleteAsync(mediaId: String) {
        PlainRust.mainMutate("deleteVideoPlayProgress(mediaId: ${gql(mediaId)})")
    }

    private fun JsonElement.progress(): VideoPlayProgress = JsonHelper.jsonDecodeFromElement<VideoPlayProgress>(this)
}
