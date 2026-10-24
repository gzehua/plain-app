package com.ismartcoding.plain.platform.media

import com.ismartcoding.plain.platform.system.ForceVideoDecoder

import coil3.request.ImageRequest

actual fun ImageRequest.Builder.applyForceVideoDecoder(force: Boolean): ImageRequest.Builder {
    if (force) {
        decoderFactory(ForceVideoDecoder.Factory())
    }
    return this
}
