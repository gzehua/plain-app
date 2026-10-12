package com.ismartcoding.plain.rust.bridge

internal expect object RustPeerStatusBridge {
    fun action(commandJson: String): String
}