package com.ismartcoding.plain.models

import kotlinx.serialization.Serializable

@Serializable
enum class MergeTaskStatus {
    NONE,
    STARTED,
    MERGING,
    DONE,
    FAILED,
}
