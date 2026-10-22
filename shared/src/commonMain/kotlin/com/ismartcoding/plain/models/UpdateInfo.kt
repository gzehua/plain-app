package com.ismartcoding.plain.models

import kotlinx.serialization.Serializable

@Serializable
data class UpdateInfo(
    val newVersion: String,
    val checkUpdateTime: Long,
    val publishDate: String,
    val log: String,
    val downloadUrl: String,
    val size: Long,
    val downloadedApkPath: String,
)
