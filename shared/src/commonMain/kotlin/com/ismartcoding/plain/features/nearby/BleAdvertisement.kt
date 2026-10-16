package com.ismartcoding.plain.features.nearby

import kotlinx.serialization.Serializable

@Serializable
data class BleAdvertisement(
    val shortId: String,
    val awareSupported: Boolean,
    val awareRunning: Boolean,
)
