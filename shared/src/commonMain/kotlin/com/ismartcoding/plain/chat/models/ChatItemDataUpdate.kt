package com.ismartcoding.plain.chat.models

import com.ismartcoding.plain.lib.TimeHelper
import kotlin.time.Instant

data class ChatItemDataUpdate(
    var id: String,
    var content: MessageContent,
    val updatedAt: Instant = TimeHelper.now(),
)
