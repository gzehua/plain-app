package com.ismartcoding.plain.features.files

import kotlinx.serialization.Serializable

@Serializable
data class FileTaskOp(val src: String, val dst: String, val overwrite: Boolean = false)
