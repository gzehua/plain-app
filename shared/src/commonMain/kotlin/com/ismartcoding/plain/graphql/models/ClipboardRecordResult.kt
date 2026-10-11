package com.ismartcoding.plain.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class ClipboardRecordResult(
    var inserted: Boolean,
    var item: ClipboardItem,
)
