package com.ismartcoding.plain.rust.host.provider

import kotlinx.serialization.Serializable

@Serializable
internal data class FileMetadataFacts(
    val size: Long,
    val mimeType: String,
)
