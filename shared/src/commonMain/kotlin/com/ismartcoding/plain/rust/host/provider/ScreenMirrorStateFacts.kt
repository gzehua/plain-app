package com.ismartcoding.plain.rust.host.provider

import com.ismartcoding.plain.models.ScreenMirrorVideoCodec
import kotlinx.serialization.Serializable

@Serializable
internal data class ScreenMirrorStateFacts(
    val running: Boolean,
    val controlEnabled: Boolean,
    val codec: ScreenMirrorVideoCodec?,
)
