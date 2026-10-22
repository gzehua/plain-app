package com.ismartcoding.plain.rust.native

internal actual object RustPrefsBridge {
    init { System.loadLibrary("plain_rust") }

    private external fun openNative(dataDir: String, cacheDir: String)

    actual fun open(dataDir: String, cacheDir: String) = openNative(dataDir, cacheDir)
}
