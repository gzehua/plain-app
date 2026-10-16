package com.ismartcoding.plain.features.images.search

import kotlinx.serialization.Serializable

@Serializable
internal data class ImageCatalogSnapshot(val revision: String, val total: Int)
