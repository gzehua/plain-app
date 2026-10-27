package com.ismartcoding.plain.features.nearby

import kotlinx.serialization.Serializable

@Serializable
data class MdnsSnapshot(
    val runtimeId: String?,
    val revision: Long,
    val receiver: Boolean,
    val running: Boolean,
    val scanning: Boolean,
    val published: Boolean,
    val manualScanning: Boolean,
    val debugSessionCount: Int,
    val capturing: Boolean,
    val services: List<MdnsServiceSnapshot>,
    val packetsIn: List<MdnsPacketLog>,
    val packetsOut: List<MdnsPacketLog>,
)
