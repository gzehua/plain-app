package com.ismartcoding.plain.features.images.editor

import com.ismartcoding.plain.util.ID
import com.ismartcoding.plain.api.graphql.models.ImageEditorProject
import kotlin.time.Instant

data class ImageEditorProjectSummary(
    val id: ID,
    val thumbnail: String?,
    val canvasWidth: Int,
    val canvasHeight: Int,
    val layerCount: Int,
    val updatedAt: Instant,
)

fun ImageEditorProject.toSummary(): ImageEditorProjectSummary {
    return ImageEditorProjectSummary(
        ID(id),
        thumbnail,
        canvasWidth,
        canvasHeight,
        layerCount,
        updatedAt,
    )
}
