package com.ismartcoding.plain.platform.media

import com.ismartcoding.plain.appContext
import coil3.SingletonImageLoader

fun clearImageMemoryCache() {
    SingletonImageLoader.get(appContext).memoryCache?.clear()
}
