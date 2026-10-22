package com.ismartcoding.plain.ui.media.extensions


import com.ismartcoding.plain.models.File as FileEntry

import com.ismartcoding.plain.ui.media.viewer.PreviewItem

fun FileEntry.toPreviewItem(): PreviewItem {
    return PreviewItem(id = path, path = path, size = size, mediaId = mediaId.orEmpty(), data = this)
}
