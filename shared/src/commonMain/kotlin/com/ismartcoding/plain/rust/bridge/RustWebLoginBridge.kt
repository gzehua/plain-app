package com.ismartcoding.plain.rust.bridge

internal expect object RustWebLoginBridge {
    fun decide(requestId: String, allowed: Boolean): String
}
