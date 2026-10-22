package com.ismartcoding.plain.rust.host

import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

@Serializable
internal data class RustHostRequest(val id: Long, val method: String, val params: JsonObject)

@Serializable
internal data class RustHostReply<T>(val id: Long, val result: T)

@Serializable
private data class RustHostError(val id: Long, val error: String)

internal fun encodeHostError(id: Long, error: Exception): String =
    JsonHelper.jsonEncode(RustHostError(id, error.message ?: "Host operation failed"))

@Serializable
internal data class PathRequest(val path: String)

@Serializable
internal data class PathsRequest(val paths: List<String>)

@Serializable
internal data class IdRequest(val id: String)

@Serializable
internal data class IdsRequest(val ids: List<String>)
