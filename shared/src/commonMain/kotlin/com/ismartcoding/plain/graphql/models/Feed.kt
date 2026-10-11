package com.ismartcoding.plain.graphql.models

import com.ismartcoding.plain.lib.TimeHelper
import com.ismartcoding.plain.lib.generateId
import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Feed(
    @kotlinx.serialization.Required override var id: String = generateId(),
    @kotlinx.serialization.Required var name: String = "",
    @kotlinx.serialization.Required var url: String = "",
    @kotlinx.serialization.Required var fetchContent: Boolean = false,
    @kotlinx.serialization.Required var logo: String = "",
    @kotlinx.serialization.Required var lastSyncAt: Instant? = null,
    @kotlinx.serialization.Required var lastError: FeedError = FeedError(),
    @kotlinx.serialization.Transient var entryCount: Int = 0,
    @kotlinx.serialization.Required var createdAt: Instant = TimeHelper.now(),
    @kotlinx.serialization.Required var updatedAt: Instant = TimeHelper.now(),
) : com.ismartcoding.plain.db.IData {
    /** True when the last sync attempt failed. */
    val hasSyncError: Boolean
        get() = lastError.code.isNotEmpty()
}
