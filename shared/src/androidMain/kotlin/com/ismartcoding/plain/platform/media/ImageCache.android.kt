package com.ismartcoding.plain.platform.media

import com.ismartcoding.plain.appContext

fun clearImageMemoryCache() {
    coil3.SingletonImageLoader.get(appContext).memoryCache?.clear()
}
