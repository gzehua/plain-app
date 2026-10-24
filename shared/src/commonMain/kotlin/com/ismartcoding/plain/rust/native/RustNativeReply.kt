package com.ismartcoding.plain.rust.native

import kotlinx.serialization.Serializable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

import com.ismartcoding.plain.lib.JsonHelper

internal object RustNativeReply {
    fun value(reply: String): String {
        val result = JsonHelper.jsonDecode<BridgeReply>(reply)
        result.error?.let { error(it) }
        return requireNotNull(result.value) { "Rust bridge reply missing value" }
    }
}

@Serializable
private data class BridgeReply(val value: String? = null, val error: String? = null)

internal inline fun <reified T : Any> nativeValue(response: String): T {
    val reply = JsonHelper.jsonDecode<NativeManagementReply<T>>(response)
    reply.error?.let { error(it) }
    return requireNotNull(reply.value) { "Native management response missing value" }
}

internal suspend inline fun <reified T : Any> nativeCall(crossinline request: () -> String): T =
    withContext(Dispatchers.IO) { nativeValue<T>(request()) }

internal suspend inline fun nativeCertificate(crossinline request: () -> String): ByteArray =
    nativeCall<List<Int>>(request).map { it.toByte() }.toByteArray()

@Serializable
internal data class NativeManagementReply<T>(val value: T? = null, val error: String? = null)
