package com.ismartcoding.plain.rust.bridge

internal actual object RustDlnaBridge {
    init { System.loadLibrary("plain_rust") }
    private external fun receiverNative(commandJson: String): String
    actual fun receiver(commandJson: String): String = receiverNative(commandJson)
}
