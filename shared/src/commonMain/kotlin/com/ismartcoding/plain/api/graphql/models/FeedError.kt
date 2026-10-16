package com.ismartcoding.plain.api.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class FeedError(
    @kotlinx.serialization.Required var code: String = "",
    @kotlinx.serialization.Required var detail: String = "",
)
