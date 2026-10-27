package com.ismartcoding.plain.api.graphql.models

import com.ismartcoding.plain.models.MergeTaskStatus
import kotlinx.serialization.Serializable

@Serializable
data class MergeResult(
    val status: MergeTaskStatus,
    val error: String? = null,
)
