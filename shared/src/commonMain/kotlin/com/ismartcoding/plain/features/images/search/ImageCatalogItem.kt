package com.ismartcoding.plain.features.images.search

import kotlinx.serialization.Serializable

@Serializable
internal data class ImageCatalogItem(val id: String, val path: String)
