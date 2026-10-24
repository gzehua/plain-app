package com.ismartcoding.plain.platform.system

import coil3.svg.SvgDecoder
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.network.NetworkFetcher
import coil3.request.crossfade
import okio.Path.Companion.toPath

fun newImageLoader(context: PlatformContext): ImageLoader =
    ImageLoader.Builder(context)
        .components {
            add(SvgDecoder.Factory(true))
            add(NetworkFetcher.Factory(networkClient = { IosImageNetworkClient() }))
        }
        .memoryCache {
            MemoryCache.Builder()
                .maxSizePercent(context, percent = 0.25)
                .build()
        }
        .diskCache {
            DiskCache.Builder()
                .directory(cacheDirPath().toPath() / "image_cache")
                .maxSizePercent(1.0)
                .build()
        }
        .crossfade(100)
        .build()

fun setupIosImageLoader() {
    SingletonImageLoader.setSafe { context -> newImageLoader(context) }
}
