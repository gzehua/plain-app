package com.ismartcoding.plain.rust.bridge

internal actual object RustDiscoveryBridge {
    init { System.loadLibrary("plain_rust") }
    private external fun actionNative(factsJson: String, requestJson: String): String
    actual fun action(factsJson: String, requestJson: String): String = actionNative(factsJson, requestJson)
}