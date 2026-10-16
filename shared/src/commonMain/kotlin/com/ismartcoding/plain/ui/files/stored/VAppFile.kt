package com.ismartcoding.plain.ui.files.stored

import com.ismartcoding.plain.api.graphql.models.AppFile

data class VAppFile(
    val appFile: AppFile,
    val fileName: String,
)
