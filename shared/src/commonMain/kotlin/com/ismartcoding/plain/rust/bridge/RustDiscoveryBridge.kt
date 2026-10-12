package com.ismartcoding.plain.rust.bridge

internal expect object RustDiscoveryBridge {
    fun action(factsJson: String, requestJson: String): String
}