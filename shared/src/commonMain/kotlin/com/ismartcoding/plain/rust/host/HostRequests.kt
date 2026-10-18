package com.ismartcoding.plain.rust.host

import kotlinx.serialization.Serializable

@Serializable
internal data class PathRequest(val path: String)

@Serializable
internal data class PathsRequest(val paths: List<String>)

@Serializable
internal data class IdRequest(val id: String)

@Serializable
internal data class IdsRequest(val ids: List<String>)
