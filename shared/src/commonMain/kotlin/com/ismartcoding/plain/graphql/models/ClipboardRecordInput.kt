package com.ismartcoding.plain.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class ClipboardRecordInput(
    var text: String,
    var source: String,
    var label: String,
    var sensitive: Boolean,
)
