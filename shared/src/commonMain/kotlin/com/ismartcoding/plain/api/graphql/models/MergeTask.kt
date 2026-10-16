package com.ismartcoding.plain.api.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class MergeTask(
    var status: MergeTaskStatus,
    var value: String?,
    var mergedSize: Long?,
    var error: String?,
)
