package com.ismartcoding.plain.features.nearby

import kotlinx.serialization.Serializable

@Serializable
enum class PeerStatus {
    PAIRED,
    UNPAIRED,
    CHANNEL;
}
