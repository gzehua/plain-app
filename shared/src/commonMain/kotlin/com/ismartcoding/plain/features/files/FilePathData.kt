package com.ismartcoding.plain.features.files

import kotlinx.serialization.Serializable

@Serializable
data class FilePathData(
    val rootPath: String,
    val fullPath: String,
    val selectedPath: String
)
