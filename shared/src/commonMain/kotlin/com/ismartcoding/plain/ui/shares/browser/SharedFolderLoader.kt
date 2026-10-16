package com.ismartcoding.plain.ui.shares.browser

import com.ismartcoding.plain.features.chat.models.MessageShare
import com.ismartcoding.plain.features.shares.SharedBrowseResult
import com.ismartcoding.plain.features.shares.SharedLinkClient
import kotlinx.coroutines.CancellationException

internal suspend fun fetchSharedInfoWithFallback(messageId: String, msg: MessageShare, virtualPath: String?): SharedBrowseResult? =
    try { SharedLinkClient.browse(messageId, msg, virtualPath) }
    catch (cancelled: CancellationException) { throw cancelled }
    catch (_: Exception) { null }
