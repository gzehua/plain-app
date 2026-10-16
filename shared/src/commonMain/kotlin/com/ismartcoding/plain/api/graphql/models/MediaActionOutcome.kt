package com.ismartcoding.plain.api.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class MediaActionOutcome(val affectedCount: Int, val failedIds: List<String>)
