package com.ismartcoding.plain.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class ShareAuthRecord(
    var share: ShareRecord,
    var token: String,
)
