package com.ismartcoding.plain.models

import kotlinx.serialization.Serializable

@Serializable
data class ChatItemId(val id: String)
@Serializable
data class MediaLibraryChange(val types: List<Int>)
@Serializable
data class MediaScanProgress(val indexed: Int, val pending: Int? = null, val total: Int? = null, val state: String, val error: String? = null)
