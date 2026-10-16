package com.ismartcoding.plain.features.media.actions

import kotlinx.serialization.Serializable

@Serializable
internal data class MediaActionFinished(val type: Int, val action: String, val ids: List<String>)
