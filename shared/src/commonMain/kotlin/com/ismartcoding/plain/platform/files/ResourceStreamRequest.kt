package com.ismartcoding.plain.platform.files

import kotlinx.serialization.Serializable

@Serializable
internal data class ResourceStreamRequest(val path: String, val file: Boolean = false)
