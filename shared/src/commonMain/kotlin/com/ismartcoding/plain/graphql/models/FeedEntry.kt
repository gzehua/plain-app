package com.ismartcoding.plain.graphql.models

import com.ismartcoding.plain.lib.TimeHelper
import com.ismartcoding.plain.lib.generateId
import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class FeedEntry(
    @kotlinx.serialization.Required var feedId: String = "",
    @kotlinx.serialization.Required override var id: String = generateId(),
    @kotlinx.serialization.Required var title: String = "",
    @kotlinx.serialization.Required var url: String = "",
    @kotlinx.serialization.Required var image: String = "",
    @kotlinx.serialization.Required var description: String = "",
    @kotlinx.serialization.Required var author: String = "",
    @kotlinx.serialization.Required var content: String = "",
    @kotlinx.serialization.Required var rawId: String = "",
    @kotlinx.serialization.Required var read: Boolean = false,
    @kotlinx.serialization.Required var publishedAt: Instant = TimeHelper.now(),
    @kotlinx.serialization.Required var createdAt: Instant = TimeHelper.now(),
    @kotlinx.serialization.Required var updatedAt: Instant = TimeHelper.now(),
    var tags: List<Tag> = emptyList(),
    var feed: Feed? = null,
) : com.ismartcoding.plain.db.IData {
    fun getSummary(): String {
        val regex = Regex("!\\[.*?\\]\\(.*?\\)|!\\[.*?\\]\\[.*?\\]|<img.*?>", RegexOption.IGNORE_CASE)
        return description.replace(regex, "🖼").replace("\n", "").replaceFirst("^\\s*".toRegex(), "")
    }

    // Heuristic: feeds that ship full articles (content:encoded merged into
    // description) produce long bodies; summary feeds stay short. No web fetch needed.
    val isFullContent: Boolean
        get() = description.length >= FULL_CONTENT_MIN_LENGTH
}

private const val FULL_CONTENT_MIN_LENGTH = 500
