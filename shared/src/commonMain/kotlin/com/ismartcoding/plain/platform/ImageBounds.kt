package com.ismartcoding.plain.platform

import kotlinx.serialization.Serializable

@Serializable
internal data class ImageBounds(val width: Int, val height: Int)

internal expect fun readImageBounds(path: String): ImageBounds
