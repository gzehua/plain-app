package com.ismartcoding.plain.rust.bridge

import kotlinx.serialization.Serializable

import com.ismartcoding.plain.lib.JsonHelper

internal object RustBridgeReply {
    fun value(reply: String): String {
        val result = JsonHelper.jsonDecode<BridgeReply>(reply)
        result.error?.let { error(it) }
        return requireNotNull(result.value) { "Rust bridge reply missing value" }
    }
}

@Serializable
private data class BridgeReply(val value: String? = null, val error: String? = null)
