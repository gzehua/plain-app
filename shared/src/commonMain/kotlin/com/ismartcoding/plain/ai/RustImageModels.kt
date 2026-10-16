package com.ismartcoding.plain.ai

import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.lib.sendEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

internal object RustImageModels {
    private val projectionLock = com.ismartcoding.plain.platform.PlatformLock()
    private var version = -1L
    private val state = MutableStateFlow(ImageModelsSnapshot())
    val snapshot = state.asStateFlow()
    suspend fun call(command: ImageModelsCommand) {
        val mutation = when (command) {
            ImageModelsCommand.Restore -> "restoreImageSearch"
            ImageModelsCommand.Enable -> "enableImageSearch"
            ImageModelsCommand.Disable -> "disableImageSearch"
            ImageModelsCommand.Cancel -> "cancelImageModelDownload"
            ImageModelsCommand.Release -> "releaseImageModelMemory"
        }
        PlainRust.mainMutate(mutation)
        refresh()
    }
    suspend fun refresh() {
        apply(PlainRust.mainQuery("imageModelsState { version status downloadProgress errorMessage modelSize modelDir isIndexing totalImages indexedImages }").getValue("imageModelsState").jsonObject)
    }
    fun apply(value: JsonObject) {
        val next = JsonHelper.jsonDecodeFromElement<ImageModelsSnapshot>(value)
        projectionLock.withLock {
            if (next.version < version) return@withLock
            version = next.version
            state.value = next
            sendEvent(HImageSearchStatusChangedEvent(next.status,next.downloadProgress,next.errorMessage))
            sendEvent(HImageIndexProgressEvent(next.totalImages,next.indexedImages,next.isIndexing))
        }
    }
}
