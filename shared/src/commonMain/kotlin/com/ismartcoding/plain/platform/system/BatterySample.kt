package com.ismartcoding.plain.platform.system

import kotlinx.serialization.Serializable

@Serializable
internal data class BatterySample(
    val level: Double?,
    val scale: Double?,
    val charging: Boolean,
    val temperatureTenths: Int? = null,
)

internal expect fun readBatterySample(): BatterySample
