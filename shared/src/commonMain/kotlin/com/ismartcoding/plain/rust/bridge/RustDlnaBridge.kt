package com.ismartcoding.plain.rust.bridge

internal expect object RustDlnaBridge {
    fun receiver(commandJson: String): String
}
