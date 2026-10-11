package com.ismartcoding.plain.platform

import com.ismartcoding.plain.db.*
import com.ismartcoding.plain.features.file.FileSortBy
import com.ismartcoding.plain.features.share.ShareManager
import com.ismartcoding.plain.TempData
import com.ismartcoding.plain.preferences.UserPrefs
import com.ismartcoding.plain.lib.withIO

internal suspend fun buildPlatformShareContent(share: DShare, paths: List<String>): DMessageContent = withIO {
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
    DMessageContent(
        MessageType.SHARE,
        DMessageShare(
            shareId = share.id,
            urlToken = ShareManager.sharedToken(share.id),
            peerInfo = DSharePeerInfo(
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
