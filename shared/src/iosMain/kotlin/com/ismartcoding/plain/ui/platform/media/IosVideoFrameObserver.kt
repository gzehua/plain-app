package com.ismartcoding.plain.ui.platform.media

import platform.AVFoundation.AVPlayerLayer

interface IosVideoFrameObserver {
    fun observe(layer: AVPlayerLayer, onReady: () -> Unit): IosVideoFrameObservation
}

interface IosVideoFrameObservation {
    fun close()
}
