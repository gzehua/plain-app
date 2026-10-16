package com.ismartcoding.plain.features.files

import com.ismartcoding.plain.features.files.FilesType

class FolderOption(
    val rootPath: String,
    val fullPath: String,
    val type: FilesType,
    var title: String,
    var isChecked: Boolean = false,
    var isFavoriteFolder: Boolean = false
)
