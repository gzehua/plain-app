package com.ismartcoding.plain.platform.media

import com.ismartcoding.plain.features.screenmirror.ScreenMirrorControlInput
import com.ismartcoding.plain.features.screenmirror.ScreenMirrorMode
import com.ismartcoding.plain.models.ScreenMirrorVideoCodec

/**
 * Whether the screen mirror service is currently running and producing frames.
 */
expect fun isScreenMirrorRunning(): Boolean

/**
 * Returns the video codec advertised by the active screen mirror pipeline, or
 * null if the pipeline is not running or the codec is not yet known.
 */
expect suspend fun getScreenMirrorVideoCodec(): ScreenMirrorVideoCodec?

/**
 * Whether the accessibility service (used for screen mirror control) is enabled.
 */
expect fun isScreenMirrorControlEnabled(): Boolean

/**
 * Update the active screen mirror quality at runtime. No-op if the service is
 * not running or the platform has no screen mirror capability.
 */
expect fun onScreenMirrorQualityChanged(mode: ScreenMirrorMode)

/**
 * Stop the screen mirror service if it is running. Safe to call when not running.
 */
expect fun stopScreenMirror()

/**
 * Dispatch a remote-control event to the accessibility service. Returns true on
 * success, false if the service is not enabled or the platform doesn't support
 * screen mirror control.
 */
expect fun dispatchScreenMirrorControl(input: ScreenMirrorControlInput): Boolean

/**
 * Request an immediate IDR keyframe from the running screen mirror encoder.
 * Used by the web client to recover from packet loss or decoder errors.
 * No-op if the pipeline is not running.
 */
expect fun requestScreenMirrorKeyFrame()


/**
 * Force-release any in-flight streamed touch gesture. Called when the last
 * web client disconnects so a press interrupted mid-stroke cannot linger.
 */
expect fun resetScreenMirrorTouchStream()
