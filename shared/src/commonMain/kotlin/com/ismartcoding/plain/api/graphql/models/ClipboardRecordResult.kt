package com.ismartcoding.plain.api.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class ClipboardRecordResult(
    var inserted: Boolean,
    var item: ClipboardItem,
)
