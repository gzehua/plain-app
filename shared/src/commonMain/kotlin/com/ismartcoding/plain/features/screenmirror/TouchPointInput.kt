package com.ismartcoding.plain.features.screenmirror

import kotlinx.serialization.Serializable

@Serializable
data class TouchPointInput(
    val x: Float,
    val y: Float,
    val tMs: Int,
)
