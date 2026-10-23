package com.ismartcoding.plain.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.Required

@Serializable
data class FeedError(
    @Required var code: String = "",
    @Required var detail: String = "",
)
