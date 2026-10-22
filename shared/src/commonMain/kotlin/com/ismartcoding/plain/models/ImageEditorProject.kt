package com.ismartcoding.plain.models

import com.ismartcoding.plain.lib.TimeHelper
import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class ImageEditorProject(
    var id: String,
    @kotlinx.serialization.Required var stateB64: String = "",
    @kotlinx.serialization.Required var thumbnail: String? = null,
    @kotlinx.serialization.Required var canvasWidth: Int = 0,
    @kotlinx.serialization.Required var canvasHeight: Int = 0,
    @kotlinx.serialization.Required var layerCount: Int = 0,
    @kotlinx.serialization.Required var createdAt: Instant = TimeHelper.now(),
    @kotlinx.serialization.Required var updatedAt: Instant = TimeHelper.now(),
)
