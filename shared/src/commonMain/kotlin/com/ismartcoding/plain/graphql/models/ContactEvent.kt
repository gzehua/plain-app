package com.ismartcoding.plain.graphql.models

import com.ismartcoding.plain.enums.EventType
import kotlinx.serialization.Serializable

@Serializable
data class ContactEvent(
    var value: String,
    var type: EventType,
    var label: String,
)
