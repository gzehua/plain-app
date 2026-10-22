package com.ismartcoding.plain.models

import kotlinx.serialization.Serializable

@Serializable
data class DeviceStatus(
    var uptimeSec: Long,
    var batteryLevel: Int?,
    var charging: Boolean,
    var temperatures: List<Temperature>,
    var cpuUsage: Double,
    var memoryAvailable: Long?,
    var storageAvailable: Long,
)
