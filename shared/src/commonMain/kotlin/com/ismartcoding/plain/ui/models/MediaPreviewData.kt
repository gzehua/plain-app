package com.ismartcoding.plain.ui.models

import androidx.compose.ui.unit.toSize
import com.ismartcoding.plain.chat.models.MessageFile
import com.ismartcoding.plain.chat.models.MessageFiles
import com.ismartcoding.plain.chat.models.MessageImages
import com.ismartcoding.plain.data.DImage
import com.ismartcoding.plain.data.DVideo
import com.ismartcoding.plain.extensions.getFinalPath
import com.ismartcoding.plain.graphql.models.Image
import com.ismartcoding.plain.graphql.models.Video
import com.ismartcoding.plain.lib.extensions.isImageFast
import com.ismartcoding.plain.lib.extensions.isVideoFast
import com.ismartcoding.plain.ui.components.mediaviewer.PreviewItem
import com.ismartcoding.plain.ui.components.mediaviewer.previewer.TransformItemState

object MediaPreviewData {
    var items = listOf<PreviewItem>()

    fun setDataAsync(
        itemState: TransformItemState,
        chatItems: List<VChat>,
        m: MessageFile
    ) {
        val newItems = mutableListOf<MessageFile>()
        chatItems.forEach { item ->
            if (item.value is MessageImages) {
                newItems.addAll((item.value as MessageImages).items)
            } else if (item.value is MessageFiles) {
                newItems.addAll((item.value as MessageFiles).items.filter { it.fileName.isVideoFast() || it.fileName.isImageFast() })
            }
        }
        items = newItems.map { f ->
            PreviewItem(f.id, f.uri.getFinalPath(), f.size, data = f)
        }
        items.find { it.id == m.id }?.let {
            it.initAsync(m)
            itemState.intrinsicSize = it.intrinsicSize.toSize()
        }
    }

    fun setDataAsync(
        itemState: TransformItemState,
        items: List<DImage>,
        m: DImage
    ) {
        MediaPreviewData.items = items.map { f ->
            PreviewItem(f.id, f.path, f.size, mediaId = f.id, data = f)
        }
        MediaPreviewData.items.find { it.id == m.id }?.let {
            it.initAsync(m)
            itemState.intrinsicSize = it.intrinsicSize.toSize()
        }
    }

    fun setDataAsync(
        itemState: TransformItemState,
        items: List<DVideo>,
        m: DVideo
    ) {
        MediaPreviewData.items = items.map { f ->
            PreviewItem(f.id, f.path, f.size, mediaId = f.id, data = f)
        }
        MediaPreviewData.items.find { it.id == m.id }?.let {
            it.initAsync(m)
            itemState.intrinsicSize = it.intrinsicSize.toSize()
        }
    }

    fun setDataAsync(
        itemState: TransformItemState,
        previewItems: List<PreviewItem>,
        selectedItem: PreviewItem
    ) {
        MediaPreviewData.items = previewItems
        items.find { it.id == selectedItem.id }?.let {
            if (it.isVideo()) {
                it.initVideoAsync()
            } else if (it.isImage()) {
                it.initImageAsync()
            }
            itemState.intrinsicSize = it.intrinsicSize.toSize()
        }
    }
}
