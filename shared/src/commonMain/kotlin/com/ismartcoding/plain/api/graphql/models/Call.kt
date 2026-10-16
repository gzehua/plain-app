package com.ismartcoding.plain.api.graphql.models

import com.ismartcoding.plain.features.telephony.CallType
import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Call(
    override var id: String,
    var number: String,
    var name: String,
    var photoId: String = "",
    var startedAt: Instant,
    var durationSec: Int,
    var type: CallType,
    var accountId: String,
    var geo: PhoneGeo? = null,
    var typeCode: Int = 0,
    var photoUri: String = "",
) : com.ismartcoding.plain.db.IData
