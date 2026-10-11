package com.ismartcoding.plain.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class SmsAttachment(
    var path: String,
    var contentType: String,
    var name: String,
)
