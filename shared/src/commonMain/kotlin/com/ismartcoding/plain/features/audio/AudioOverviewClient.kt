package com.ismartcoding.plain.features.audio

import com.ismartcoding.plain.api.graphql.Query
import com.ismartcoding.plain.rust.RustEventHandler
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi
import kotlinx.serialization.Serializable
import com.ismartcoding.plain.models.AudioOverview
import com.ismartcoding.plain.app.events.EventType
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.platform.system.DataType
import kotlinx.coroutines.flow.filter
import kotlinx.serialization.json.*

object AudioOverviewClient {
    suspend fun read(): AudioOverview = JsonHelper.jsonDecodeFromElement(
        RustGraphqlApi.graphql(Query.audioOverview()).getValue("audioOverview"),
    )

    val updates get() = RustEventHandler.events.filter { updatesOverview(it.type, it.payload) }

    internal fun updatesOverview(type: EventType, payload: String): Boolean = when (type) {
        EventType.AUDIO_LIBRARY_UPDATED -> true
        EventType.MEDIA_LIBRARY_UPDATED -> JsonHelper.jsonDecode<MediaLibraryUpdate>(payload).types?.contains(DataType.AUDIO.value) == true
        EventType.MEDIA_SCAN_PROGRESS -> when (JsonHelper.jsonDecode<MediaScanUpdate>(payload).state) {
            "IDLE", "FAILED", "STOPPED", "PAUSED" -> true
            else -> false
        }
        else -> false
    }
}

@Serializable
private data class MediaLibraryUpdate(val types: List<Int>? = null)

@Serializable
private data class MediaScanUpdate(val state: String? = null)
