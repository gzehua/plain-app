package com.ismartcoding.plain.rust.event

import com.ismartcoding.plain.app.events.EventType
import com.ismartcoding.plain.app.events.HostEventData
import kotlinx.serialization.Serializable

@Serializable
internal data class HostEventCapabilities(
    val textTypes: Set<String>,
    val binaryTypes: Set<String>,
) {
    fun accepts(type: EventType, data: HostEventData): Boolean = type.name in when (data) {
        is HostEventData.Text -> textTypes
        is HostEventData.Binary -> binaryTypes
    }
}
