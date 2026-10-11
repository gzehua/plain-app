package com.ismartcoding.plain.graphql.models

import com.ismartcoding.plain.enums.CallType
import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Call(
    var id: String,
    var number: String,
    var name: String,
    var photoId: String,
    var startedAt: Instant,
    var durationSec: Int,
    var type: CallType,
    var accountId: String,
    var geo: PhoneGeo?,
)
