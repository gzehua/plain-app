package com.ismartcoding.plain.rust.host.files

import com.ismartcoding.plain.features.files.DriveType
import kotlinx.serialization.Serializable

@Serializable
internal data class FileFacts(
    val name: String,
    val path: String,
    val mediaId: String,
    val createdAt: Long?,
    val updatedAt: Long,
    val size: Long,
    val isDir: Boolean,
    val childCount: Int,
)

@Serializable
internal data class MountFacts(val name: String, val path: String, val totalBytes: Long, val freeBytes: Long, val driveType: DriveType)
