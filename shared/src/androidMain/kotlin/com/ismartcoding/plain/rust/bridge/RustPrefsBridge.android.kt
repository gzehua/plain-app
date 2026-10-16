package com.ismartcoding.plain.rust.bridge

internal actual object RustPrefsBridge {
    init { System.loadLibrary("plain_rust") }

    private external fun openNative(dataDir: String, cacheDir: String)
    private external fun userSnapshotNative(): String

    actual fun open(dataDir: String, cacheDir: String) = openNative(dataDir, cacheDir)
    actual fun userSnapshot(): String = userSnapshotNative()
}
