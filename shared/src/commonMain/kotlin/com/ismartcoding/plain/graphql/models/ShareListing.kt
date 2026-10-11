package com.ismartcoding.plain.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class ShareListing(
    var share: ShareRecord,
    var entries: List<Root>,
)
