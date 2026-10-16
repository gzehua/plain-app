package com.ismartcoding.plain.api.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class ShareListing(
    var share: ShareRecord,
    var entries: List<Root>,
)
