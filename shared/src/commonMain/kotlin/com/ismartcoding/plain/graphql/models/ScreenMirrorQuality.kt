package com.ismartcoding.plain.graphql.models

import com.ismartcoding.plain.enums.ScreenMirrorMode
import kotlinx.serialization.Serializable

@Serializable
data class ScreenMirrorQuality(
    var mode: ScreenMirrorMode,
    var resolution: Int,
)
