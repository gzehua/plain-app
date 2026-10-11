package com.ismartcoding.plain.graphql.models

import com.ismartcoding.plain.enums.DriveType
import kotlinx.serialization.Serializable

@Serializable
data class Mount(
    var id: String,
    var name: String,
    var path: String,
    var mountPoint: String,
    var fsType: String,
    var totalBytes: Long,
    var usedBytes: Long,
    var freeBytes: Long,
    var remote: Boolean,
    var alias: String,
    var driveType: DriveType,
    var diskId: String,
)
