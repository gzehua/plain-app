package com.ismartcoding.plain.features.system

import kotlinx.serialization.Serializable

@Serializable
data class MediaBucketItemFacts(
    val id: String,
    val name: String,
    val size: Long,
    val path: String,
)
