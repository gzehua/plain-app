package com.ismartcoding.plain.models

import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class ImageEditorProjectSummary(
    var id: String,
    var thumbnail: String?,
    var canvasWidth: Int,
    var canvasHeight: Int,
    var layerCount: Int,
    var updatedAt: Instant,
)
