package com.ismartcoding.plain.ui.models

import com.ismartcoding.plain.graphql.models.AppFileRecord

data class VAppFile(
    val appFile: AppFileRecord,
    val fileName: String,
)
