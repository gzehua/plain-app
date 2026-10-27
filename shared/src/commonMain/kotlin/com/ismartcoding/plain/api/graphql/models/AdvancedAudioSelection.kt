package com.ismartcoding.plain.api.graphql.models

import com.ismartcoding.plain.models.AudioItem
import kotlinx.serialization.Serializable

@Serializable
data class AdvancedAudioSelection(val track: AudioItem?)
