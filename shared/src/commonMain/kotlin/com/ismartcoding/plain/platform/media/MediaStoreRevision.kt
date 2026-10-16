package com.ismartcoding.plain.platform.media

import kotlinx.serialization.Serializable

@Serializable
internal data class MediaVolumeRevision(val volume: String, val version: String, val generation: Long)

internal expect fun mediaStoreRevision(): List<MediaVolumeRevision>?
