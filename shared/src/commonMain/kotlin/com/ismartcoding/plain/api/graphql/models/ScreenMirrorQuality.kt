package com.ismartcoding.plain.api.graphql.models

import com.ismartcoding.plain.features.screenmirror.ScreenMirrorMode
import kotlinx.serialization.Serializable

@Serializable
data class ScreenMirrorQuality(
    var mode: ScreenMirrorMode,
    var resolution: Int,
)
