package com.ismartcoding.plain.api.graphql.models

import kotlinx.serialization.Serializable

@Serializable
enum class MergeTaskStatus {
    NONE,
    STARTED,
    MERGING,
    DONE,
    FAILED,
}
