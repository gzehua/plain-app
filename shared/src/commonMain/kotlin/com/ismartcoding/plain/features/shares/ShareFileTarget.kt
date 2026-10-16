package com.ismartcoding.plain.features.shares

import kotlinx.serialization.Serializable

@Serializable
data class ShareFileTarget(
    val entry: SharedFileDto,
    val writeDir: String,
    val storeToDownloads: Boolean,
    val entryName: String,
)
