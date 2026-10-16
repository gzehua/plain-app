package com.ismartcoding.plain.rust.http

import com.ismartcoding.plain.appContext

internal actual fun webAssetsSource(): RustWebAssetsSource {
    val info = appContext.applicationInfo
    return RustWebAssetsSource(
        archives = listOf(info.sourceDir) + info.splitSourceDirs.orEmpty(),
        prefix = "assets/web/",
    )
}
