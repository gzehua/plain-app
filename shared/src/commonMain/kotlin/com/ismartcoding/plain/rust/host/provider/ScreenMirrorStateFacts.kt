package com.ismartcoding.plain.rust.host.provider

import kotlinx.serialization.Serializable

@Serializable
internal data class ScreenMirrorStateFacts(
    val running: Boolean,
    val controlEnabled: Boolean,
    val codec: ScreenMirrorCodecFacts?,
)
