package com.ismartcoding.plain.features.images.search

import kotlinx.serialization.Serializable

@Serializable
internal data class ImageCatalogPage(val revision: String, val items: List<ImageCatalogItem>, val nextCursor: String, val done: Boolean)
