package com.ismartcoding.plain.models

import kotlinx.serialization.Serializable

@Serializable
data class MergeTask(
    var status: MergeTaskStatus,
    var value: String?,
    var mergedSize: Long?,
    var error: String?,
)
