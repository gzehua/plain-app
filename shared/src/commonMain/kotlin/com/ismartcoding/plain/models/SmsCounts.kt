package com.ismartcoding.plain.models

import kotlinx.serialization.Serializable

@Serializable
data class SmsCounts(
    var total: Int,
    var inbox: Int,
    var sent: Int,
    var drafts: Int,
)
