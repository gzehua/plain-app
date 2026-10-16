package com.ismartcoding.plain.rust.http

import platform.Foundation.NSBundle

internal actual fun webAssetsSource(): RustWebAssetsSource =
    RustWebAssetsSource(directory = NSBundle.mainBundle.resourcePath?.let { "$it/web" }.orEmpty())
