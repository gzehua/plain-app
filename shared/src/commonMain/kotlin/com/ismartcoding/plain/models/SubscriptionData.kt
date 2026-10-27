package com.ismartcoding.plain.models

import com.ismartcoding.plain.platform.system.DataType
import kotlinx.serialization.Serializable

@Serializable
data class ChatItemId(val id: String)

@Serializable
data class MediaLibraryChange(val types: List<DataType>)

@Serializable
data class MediaScanProgress(
    val indexed: Int,
    val pending: Int? = null,
    val total: Int? = null,
    val state: MediaScanState,
    val error: String? = null,
)
