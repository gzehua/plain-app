package com.ismartcoding.plain.api.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class ShareAuthRecord(
    var share: ShareRecord,
    var token: String,
)
