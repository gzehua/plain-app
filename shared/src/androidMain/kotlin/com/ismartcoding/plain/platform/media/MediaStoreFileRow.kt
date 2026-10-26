package com.ismartcoding.plain.platform.media

import kotlinx.serialization.Serializable

@Serializable
data class MediaStoreFileRow(val id: String, val path: String, val providerType: Int, val mimeType: String?)
