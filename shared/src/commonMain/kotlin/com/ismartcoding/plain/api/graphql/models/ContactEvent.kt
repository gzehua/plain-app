package com.ismartcoding.plain.api.graphql.models

import com.ismartcoding.plain.features.telephony.EventType
import kotlinx.serialization.Serializable

@Serializable
data class ContactEvent(
    var value: String,
    var type: EventType,
    var label: String,
    var typeCode: Int = 0,
)
