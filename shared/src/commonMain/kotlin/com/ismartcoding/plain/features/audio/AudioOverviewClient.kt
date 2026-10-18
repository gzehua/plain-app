package com.ismartcoding.plain.features.audio

import kotlinx.serialization.Serializable

import com.ismartcoding.plain.api.graphql.models.AudioOverview
import com.ismartcoding.plain.app.events.EventType
import com.ismartcoding.plain.features.media.MediaCatalogClient
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.platform.system.DataType
import com.ismartcoding.plain.rust.PlainRust
import kotlinx.coroutines.flow.filter
import kotlinx.serialization.json.*

object AudioOverviewClient {
    suspend fun read(): AudioOverview = JsonHelper.jsonDecodeFromElement(
        PlainRust.mainQuery("audioOverview { artists { name itemCount playCount samplePath } recentItems { ${MediaCatalogClient.fields(DataType.AUDIO)} } }").getValue("audioOverview"),
    )

    val updates get() = PlainRust.events.filter { updatesOverview(it.type, it.payload) }

    internal fun updatesOverview(type: String, payload: String): Boolean = when (type) {
        EventType.AUDIO_LIBRARY_UPDATED.name -> true
        EventType.MEDIA_LIBRARY_UPDATED.name -> JsonHelper.jsonDecode<MediaLibraryUpdate>(payload).types?.contains(DataType.AUDIO.value) == true
        EventType.MEDIA_SCAN_PROGRESS.name -> when (JsonHelper.jsonDecode<MediaScanUpdate>(payload).state) {
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
