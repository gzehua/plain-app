package com.ismartcoding.plain.api.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class AudioOverview(val artists: List<AudioArtist>, val recentItems: List<Audio>)
