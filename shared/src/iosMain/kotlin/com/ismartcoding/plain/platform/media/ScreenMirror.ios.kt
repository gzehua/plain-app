package com.ismartcoding.plain.platform.media

import com.ismartcoding.plain.features.screenmirror.ScreenMirrorControlInput
import com.ismartcoding.plain.features.screenmirror.ScreenMirrorMode
import com.ismartcoding.plain.api.graphql.models.ScreenMirrorVideoCodec

actual fun isScreenMirrorRunning(): Boolean = false

actual suspend fun getScreenMirrorVideoCodec(): ScreenMirrorVideoCodec? = null

actual fun isScreenMirrorControlEnabled(): Boolean = false

actual fun onScreenMirrorQualityChanged(mode: ScreenMirrorMode) = Unit

actual fun stopScreenMirror() = Unit

actual fun applyScreenMirrorQualityPreference() = Unit

actual fun dispatchScreenMirrorControl(input: ScreenMirrorControlInput): Boolean = false

actual fun requestScreenMirrorKeyFrame() = Unit


actual fun resetScreenMirrorTouchStream() = Unit
