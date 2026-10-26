package com.ismartcoding.plain.rust.host.audio

import kotlinx.coroutines.flow.*
import kotlinx.serialization.Serializable

@Serializable
internal data class MediaCatalogChange(val types: List<Int>)

internal suspend fun forwardAudioCatalogChanges(changes: Flow<Unit>, publish: suspend () -> Unit) {
    changes.conflate().collect { publish() }
}
