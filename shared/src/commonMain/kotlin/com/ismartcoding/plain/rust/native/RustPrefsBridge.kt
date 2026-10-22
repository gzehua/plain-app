package com.ismartcoding.plain.rust.native

internal expect object RustPrefsBridge {
    fun open(dataDir: String, cacheDir: String)

}
