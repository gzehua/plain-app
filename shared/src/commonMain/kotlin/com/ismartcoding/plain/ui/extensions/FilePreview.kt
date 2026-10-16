package com.ismartcoding.plain.ui.extensions


import com.ismartcoding.plain.graphql.models.File as FileEntry

import com.ismartcoding.plain.ui.components.mediaviewer.PreviewItem

fun FileEntry.toPreviewItem(): PreviewItem {
    return PreviewItem(id = path, path = path, size = size, mediaId = mediaId.orEmpty(), data = this)
}
