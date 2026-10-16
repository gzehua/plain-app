package com.ismartcoding.plain.platform.media

import kotlinx.serialization.Serializable

@Serializable
internal data class ImageCatalogSnapshot(val revision: String, val total: Int)
