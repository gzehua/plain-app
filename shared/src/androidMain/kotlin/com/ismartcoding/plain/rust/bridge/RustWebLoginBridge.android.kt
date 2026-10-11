package com.ismartcoding.plain.rust.bridge

internal actual object RustWebLoginBridge {
    init { System.loadLibrary("plain_rust") }
    private external fun decideNative(requestId: String, allowed: Boolean): String
    actual fun decide(requestId: String, allowed: Boolean): String = decideNative(requestId, allowed)
}
