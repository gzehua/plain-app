package com.ismartcoding.plain.ui.shares.browser

import com.ismartcoding.plain.features.shares.SharedFileDto
import com.ismartcoding.plain.features.shares.SharedLink
import com.ismartcoding.plain.features.shares.SharedLinkClient
import com.ismartcoding.plain.lib.extensions.isImageFast
import com.ismartcoding.plain.lib.extensions.isVideoFast
import kotlinx.coroutines.CancellationException

internal object SharedThumbCache {
    fun isThumbable(entry: SharedFileDto): Boolean = !entry.isDir && (
        entry.mimeType.startsWith("image/") || entry.mimeType.startsWith("video/") ||
            entry.name.isImageFast() || entry.name.isVideoFast())

    suspend fun thumbPath(link: SharedLink, urlToken: String, entry: SharedFileDto): String? {
        if (!isThumbable(entry)) return null
        return try { SharedLinkClient.cacheFile(link, urlToken, entry, thumbnail = true) }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { null }
    }
}
