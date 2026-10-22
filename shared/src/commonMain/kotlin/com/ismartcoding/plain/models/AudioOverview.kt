package com.ismartcoding.plain.models

import kotlinx.serialization.Serializable

@Serializable
data class AudioOverview(val artists: List<AudioArtist>, val recentItems: List<Audio>)
