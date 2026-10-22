package com.ismartcoding.plain.rust.host

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

@Serializable
internal data class RustHostRequest(val id: Long, val method: String, val params: JsonObject)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class RustHostReply(
    val id: Long,
    @EncodeDefault(EncodeDefault.Mode.NEVER) val result: JsonElement? = null,
    @EncodeDefault(EncodeDefault.Mode.NEVER) val error: String? = null,
)

@Serializable
internal data class PathRequest(val path: String)

@Serializable
internal data class PathsRequest(val paths: List<String>)

@Serializable
internal data class IdRequest(val id: String)

@Serializable
internal data class IdsRequest(val ids: List<String>)
