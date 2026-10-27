package com.ismartcoding.plain.api.graphql.models

import com.ismartcoding.plain.features.downloads.DownloadStatus
import com.ismartcoding.plain.features.shares.ShareBatchType
import com.ismartcoding.plain.features.shares.SharedLink
import com.ismartcoding.plain.features.shares.SharedFileDto
import kotlinx.serialization.Serializable

@Serializable
data class SharedDownloadRow(
    val id: String,
    val messageId: String,
    val type: ShareBatchType,
    val title: String,
    val targetDir: String,
    val link: SharedLink,
    val urlToken: String,
    val entries: List<SharedFileDto>,
    val zipName: String,
    val status: DownloadStatus,
    val error: String,
    val downloadedSize: Long,
    val totalSize: Long,
    val downloadSpeed: Long,
    val totalFiles: Long,
    val doneFiles: Long,
    val failedFiles: Long,
    val currentFile: String,
    val packing: Boolean,
    val failures: List<SharedDownloadFailure>,
)
