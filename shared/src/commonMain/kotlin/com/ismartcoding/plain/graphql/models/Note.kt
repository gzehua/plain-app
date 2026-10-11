package com.ismartcoding.plain.graphql.models

import com.ismartcoding.plain.lib.TimeHelper
import com.ismartcoding.plain.lib.generateId
import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Note(
    @kotlinx.serialization.Required override var id: String = generateId(),
    @kotlinx.serialization.Required var title: String = "",
    @kotlinx.serialization.Required var content: String = "",
    @kotlinx.serialization.Required var deletedAt: Instant? = null,
    @kotlinx.serialization.Required var createdAt: Instant = TimeHelper.now(),
    @kotlinx.serialization.Required var updatedAt: Instant = TimeHelper.now(),
) : com.ismartcoding.plain.db.IData {
    fun getSummary(): String {
        return content.replace("\n", "").replaceFirst("^\\s*".toRegex(), "")
    }
}
