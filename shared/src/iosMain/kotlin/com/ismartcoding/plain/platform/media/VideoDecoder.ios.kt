@file:OptIn(ExperimentalForeignApi::class)

package com.ismartcoding.plain.platform.media

import coil3.ImageLoader
import coil3.decode.DecodeResult
import coil3.decode.Decoder
import coil3.decode.ImageSource
import coil3.fetch.SourceFetchResult
import coil3.request.ImageRequest
import coil3.request.Options
import coil3.size.Dimension
import com.ismartcoding.plain.lib.toByteArray
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AVFoundation.AVAssetImageGenerator
import platform.AVFoundation.AVURLAsset
import platform.CoreGraphics.CGSizeMake
import platform.CoreMedia.CMTimeMake
import platform.Foundation.NSURL
import platform.UIKit.UIImage
import platform.UIKit.UIImagePNGRepresentation

actual fun ImageRequest.Builder.applyForceVideoDecoder(force: Boolean): ImageRequest.Builder {
    if (force) decoderFactory(VideoFrameDecoder.Factory())
    return this
}

private class VideoFrameDecoder(
    private val source: ImageSource,
    private val options: Options,
) : Decoder {
    override suspend fun decode(): DecodeResult {
        val asset = AVURLAsset(NSURL.fileURLWithPath(source.file().toString()), null)
        val generator = AVAssetImageGenerator(asset)
        generator.appliesPreferredTrackTransform = true
        val width = options.size.width as? Dimension.Pixels
        val height = options.size.height as? Dimension.Pixels
        if (width != null || height != null) {
            generator.maximumSize = CGSizeMake(
                (width?.px ?: Int.MAX_VALUE).toDouble(),
                (height?.px ?: Int.MAX_VALUE).toDouble(),
            )
        }
        val bytes = suspendCancellableCoroutine<ByteArray> { continuation ->
            generator.generateCGImageAsynchronouslyForTime(CMTimeMake(0, 1)) { image, _, error ->
                if (continuation.isActive) {
                    continuation.resumeWith(runCatching {
                        check(error == null) { "${error?.domain} (${error?.code}): ${error?.localizedDescription}" }
                        val frame = checkNotNull(image) { "Video frame generation returned no image" }
                        checkNotNull(UIImagePNGRepresentation(UIImage.imageWithCGImage(frame))) {
                            "Could not encode video frame"
                        }.toByteArray()
                    })
                }
            }
            continuation.invokeOnCancellation { generator.cancelAllCGImageGeneration() }
        }
        return DecodeResult(decodeIosImage(bytes), isSampled = width != null || height != null)
    }

    class Factory : Decoder.Factory {
        override fun create(result: SourceFetchResult, options: Options, imageLoader: ImageLoader): Decoder =
            VideoFrameDecoder(result.source, options)
    }
}
