package com.ismartcoding.plain.platform.system

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.disk.DiskCache
import coil3.request.crossfade
import okio.Path.Companion.toPath

internal fun newImageLoader(context: PlatformContext): ImageLoader =
    platformImageLoaderBuilder(context)
        .diskCache {
            DiskCache.Builder()
                .directory(cacheDirPath().toPath() / "image_cache")
                .maxSizePercent(1.0)
                .build()
        }
        .crossfade(100)
        .build()

internal expect fun platformImageLoaderBuilder(context: PlatformContext): ImageLoader.Builder
