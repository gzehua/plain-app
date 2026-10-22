package com.ismartcoding.plain.models

import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class PackageStatus(
    var id: String,
    var exists: Boolean,
    var updatedAt: Instant?,
)
