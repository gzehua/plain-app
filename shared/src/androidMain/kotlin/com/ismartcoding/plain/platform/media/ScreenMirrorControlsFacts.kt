package com.ismartcoding.plain.platform.media

import kotlinx.serialization.Serializable
import com.ismartcoding.plain.features.screenmirror.ScreenMirrorControlInput

@Serializable
internal data class ScreenMirrorControlsFacts(val inputs: List<ScreenMirrorControlInput>)
