package com.ismartcoding.plain.features.shares

import com.ismartcoding.plain.platform.files.listFilesInDir
import com.ismartcoding.plain.platform.files.statFile
import com.ismartcoding.plain.platform.network.getDeviceIP4

import com.ismartcoding.plain.app.TempData
import com.ismartcoding.plain.features.chat.models.MessageContent
import com.ismartcoding.plain.features.chat.models.MessageShare
import com.ismartcoding.plain.features.chat.models.MessageType
import com.ismartcoding.plain.features.chat.models.SharePeerInfo
import com.ismartcoding.plain.db.*
import com.ismartcoding.plain.features.files.FileSortBy
import com.ismartcoding.plain.features.shares.SharesClient
import com.ismartcoding.plain.api.graphql.models.ShareRecord
import com.ismartcoding.plain.lib.withIO
import com.ismartcoding.plain.preferences.UserPrefs

internal suspend fun buildPlatformShareContent(share: ShareRecord, paths: List<String>): MessageContent = withIO {
    // Single directory root: count its shallow entries; file roots: one
    // item per path. Directory sizes are not walked recursively.
    val isSingleDir = paths.size == 1 && (statFile(paths[0])?.isDir == true)
    val itemCount: Int
    val totalSize: Long
    if (isSingleDir) {
        val entries = listFilesInDir(paths[0], showHidden = false, sortBy = FileSortBy.NAME_ASC)
        itemCount = entries.size
        totalSize = entries.sumOf { if (it.isDir) 0L else it.size }
    } else {
        itemCount = paths.size
        totalSize = paths.sumOf { statFile(it)?.size ?: 0L }
    }
    MessageContent(
        MessageType.SHARE,
        MessageShare(
            shareId = share.id,
            urlToken = SharesClient.sharedToken(share.id),
            peerInfo = SharePeerInfo(
                id = TempData.clientId,
                ip = getDeviceIP4(),
                port = UserPrefs.httpsPort.value,
            ),
            name = share.name,
            itemCount = itemCount,
            totalSize = totalSize,
            expiresAt = share.expiresAt,
        ),
    )
}
