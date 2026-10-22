package com.ismartcoding.plain.models

import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Notification(
    var id: String,
    var onlyOnce: Boolean,
    var isClearable: Boolean,
    var appId: String,
    var appName: String,
    var postedAt: Instant,
    var silent: Boolean,
    var title: String,
    var body: String,
    var actions: List<String>,
    var replyActions: List<String>,
)
