package com.ismartcoding.plain.api.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class AudioArtist(val name: String, val itemCount: Int, val playCount: Long, val samplePath: String)
