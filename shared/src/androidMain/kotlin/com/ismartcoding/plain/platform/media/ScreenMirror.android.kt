package com.ismartcoding.plain.platform.media


import com.ismartcoding.plain.appContext
import com.ismartcoding.plain.features.screenmirror.ScreenMirrorControlInput
import com.ismartcoding.plain.features.screenmirror.ScreenMirrorMode
import com.ismartcoding.plain.services.PlainAccessibilityService
import com.ismartcoding.plain.services.ScreenMirrorService
import com.ismartcoding.plain.models.ScreenMirrorVideoCodec
import com.ismartcoding.plain.services.StreamTouchInjector
import com.ismartcoding.plain.services.TouchInjectThread

actual fun isScreenMirrorRunning(): Boolean =
    ScreenMirrorService.instance?.isRunning() == true

actual suspend fun getScreenMirrorVideoCodec(): ScreenMirrorVideoCodec? =
    ScreenMirrorService.instance?.let { svc ->
        svc.getPipeline()?.awaitVideoCodec()
    }

actual fun isScreenMirrorControlEnabled(): Boolean =
    PlainAccessibilityService.isEnabled()

actual fun onScreenMirrorQualityChanged(mode: ScreenMirrorMode) {
    ScreenMirrorService.instance?.onQualityChanged()
}

actual fun stopScreenMirror() {
    ScreenMirrorService.instance?.stop()
    ScreenMirrorService.instance = null
}

actual fun dispatchScreenMirrorControl(input: ScreenMirrorControlInput): Boolean {
    val service = PlainAccessibilityService.instance ?: return false
    val screenSize = PlainAccessibilityService.getScreenSize(appContext)
    service.dispatchControl(input, screenSize.x, screenSize.y)
    return true
}

actual fun requestScreenMirrorKeyFrame() {
    ScreenMirrorService.instance?.getPipeline()?.requestKeyFrame()
}


actual fun resetScreenMirrorTouchStream() {
    TouchInjectThread.handler.post {
        StreamTouchInjector.reset()
    }
}
