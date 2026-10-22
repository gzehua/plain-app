package com.ismartcoding.plain.models

import com.ismartcoding.plain.lib.TimeHelper
import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class ClipboardItem(
    var id: String,
    var text: String = "",
    var source: String = "",
    var label: String = "",
    var sensitive: Boolean = false,
    var createdAt: Instant = TimeHelper.now(),
)
