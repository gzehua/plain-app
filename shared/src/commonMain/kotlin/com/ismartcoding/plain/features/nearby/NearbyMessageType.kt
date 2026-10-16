package com.ismartcoding.plain.features.nearby

enum class NearbyMessageType {
    DISCOVER,
    DISCOVER_REPLY,
    PAIR_REQUEST,
    PAIR_RESPONSE,
    PAIR_CANCEL;

    fun toPrefix(): String {
        return "${this}:"
    }
}
