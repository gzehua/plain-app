package com.ismartcoding.plain.api.graphql.models

import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class PackageInstallPending(
    var id: String,
    var updatedAt: Instant?,
    var isNew: Boolean,
)
