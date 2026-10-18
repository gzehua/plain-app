package com.ismartcoding.plain.features.settings

import kotlinx.serialization.Serializable

@Serializable
data class AppUpdateDownload(
    val coreId: String = "",
    val taskId: String = "",
    val revision: Long = 0,
    val status: String = "IDLE",
    val progress: Int = 0,
    val filePath: String = "",
    val error: String = "",
)
