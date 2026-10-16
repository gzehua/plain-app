package com.ismartcoding.plain.platform.media

import kotlinx.serialization.Serializable

@Serializable
internal data class ScreenMirrorControlsFacts(val inputs: List<com.ismartcoding.plain.features.screenmirror.ScreenMirrorControlInput>)
