package com.ismartcoding.plain.models

import kotlinx.serialization.Serializable

@Serializable
data class FileTaskCompletedOp(
    var src: String,
    var dst: String,
)
