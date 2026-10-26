package com.ismartcoding.plain.rust.host.files

import com.ismartcoding.plain.features.files.DriveType
import kotlinx.serialization.Serializable

@Serializable
internal data class MountFacts(val name: String, val path: String, val totalBytes: Long, val freeBytes: Long, val driveType: DriveType)
