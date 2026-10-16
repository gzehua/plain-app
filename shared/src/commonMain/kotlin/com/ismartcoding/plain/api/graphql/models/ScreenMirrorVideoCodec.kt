package com.ismartcoding.plain.api.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class ScreenMirrorVideoCodec(
    var annexB: String,
    var keyFrame: String?,
)
