package com.ismartcoding.plain.features.images.search

import kotlinx.serialization.Serializable

@Serializable
enum class ImageSearchStatusType {
    UNAVAILABLE,
    DOWNLOADING,
    LOADING,
    READY,
    ERROR,
}
