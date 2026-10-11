package com.ismartcoding.plain.chat.models

import com.ismartcoding.plain.extensions.getFinalPath

suspend fun MessageFile.getPreviewPath(peer: PeerRecord?): String {
    return if (isRemoteFile()) {
        peer?.getFileUrl(parseFileId())?.let { "$it&w=200&h=200" }.orEmpty()
    } else {
        uri.getFinalPath()
    }
}
