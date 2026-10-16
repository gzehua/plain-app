package com.ismartcoding.plain.rust.host.provider

import kotlinx.serialization.Serializable
import com.ismartcoding.plain.features.files.DriveType

@Serializable
internal data class MountFacts(val name: String, val path: String, val totalBytes: Long, val freeBytes: Long, val driveType: DriveType)
