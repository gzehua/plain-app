package com.ismartcoding.plain.rust.bridge

internal actual object RustPeerStatusBridge {
    init { System.loadLibrary("plain_rust") }
    private external fun actionNative(commandJson: String): String
    actual fun action(commandJson: String): String = actionNative(commandJson)
}