package com.ismartcoding.plain.rust.bridge

internal expect object RustPrefsBridge {
    fun open(dataDir: String, cacheDir: String)

}
