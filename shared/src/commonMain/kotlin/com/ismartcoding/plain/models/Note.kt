package com.ismartcoding.plain.models

import com.ismartcoding.plain.IData
import com.ismartcoding.plain.lib.TimeHelper
import kotlin.time.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.Required

@Serializable
data class Note(
    override var id: String,
    @Required var title: String = "",
    @Required var content: String = "",
    @Required var deletedAt: Instant? = null,
    @Required var createdAt: Instant = TimeHelper.now(),
    @Required var updatedAt: Instant = TimeHelper.now(),
) : IData {
    fun getSummary(): String {
        return content.replace("\n", "").replaceFirst("^\\s*".toRegex(), "")
    }
}
