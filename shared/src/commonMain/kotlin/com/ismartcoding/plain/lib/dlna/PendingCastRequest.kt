package com.ismartcoding.plain.lib.dlna

import kotlinx.serialization.Serializable

@Serializable
data class PendingCastRequest(
    val senderIp: String,
    val senderName: String,
    val mediaUri: String,
    val mediaTitle: String,
    val mediaType: DlnaMediaType = DlnaMediaType.UNKNOWN,
    val albumArtUri: String = "",
)
