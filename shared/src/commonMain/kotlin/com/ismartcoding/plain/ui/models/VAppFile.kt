package com.ismartcoding.plain.ui.models

import com.ismartcoding.plain.graphql.models.AppFile

data class VAppFile(
    val appFile: AppFile,
    val fileName: String,
)
