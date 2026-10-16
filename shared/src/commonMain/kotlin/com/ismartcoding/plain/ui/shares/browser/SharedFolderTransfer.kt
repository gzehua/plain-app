package com.ismartcoding.plain.ui.shares.browser

import com.ismartcoding.plain.features.shares.SharedFileDto
import com.ismartcoding.plain.features.shares.SharedLink
import com.ismartcoding.plain.features.shares.SharedLinkClient
import kotlinx.coroutines.CancellationException

internal object SharedFolderTransfer {
    suspend fun downloadPreviewFile(link: SharedLink, urlToken: String, entry: SharedFileDto): String? =
        try { SharedLinkClient.cacheFile(link, urlToken, entry) }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { null }
}
