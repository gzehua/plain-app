package com.ismartcoding.plain.ui.media

import com.ismartcoding.plain.ui.chat.VChat

import com.ismartcoding.plain.api.graphql.models.Video as MediaVideo
import com.ismartcoding.plain.api.graphql.models.Image as MediaImage
import androidx.compose.ui.unit.toSize
import com.ismartcoding.plain.features.chat.models.MessageFile
import com.ismartcoding.plain.features.chat.models.MessageFiles
import com.ismartcoding.plain.features.chat.models.MessageImages
import com.ismartcoding.plain.api.graphql.models.Image
import com.ismartcoding.plain.api.graphql.models.Video
import com.ismartcoding.plain.lib.extensions.isImageFast
import com.ismartcoding.plain.lib.extensions.isVideoFast
import com.ismartcoding.plain.ui.media.viewer.PreviewItem
import com.ismartcoding.plain.ui.media.viewer.previewer.TransformItemState

object MediaPreviewData {
    var items = listOf<PreviewItem>()

    suspend fun setDataAsync(
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
        val paths = com.ismartcoding.plain.features.files.FilePaths.resolveAll(newItems.map { it.uri })
        items = newItems.mapIndexed { index, f ->
            PreviewItem(f.id, paths[index], f.size, data = f)
        }
        items.find { it.id == m.id }?.let {
            it.initAsync(m)
            itemState.intrinsicSize = it.intrinsicSize.toSize()
        }
    }

    suspend fun setDataAsync(
        itemState: TransformItemState,
        items: List<MediaImage>,
        m: MediaImage
    ) {
        MediaPreviewData.items = items.map { f ->
            PreviewItem(f.id, f.path, f.size, mediaId = f.id, data = f)
        }
        MediaPreviewData.items.find { it.id == m.id }?.let {
            it.initAsync(m)
            itemState.intrinsicSize = it.intrinsicSize.toSize()
        }
    }

    suspend fun setDataAsync(
        itemState: TransformItemState,
        items: List<MediaVideo>,
        m: MediaVideo
    ) {
        MediaPreviewData.items = items.map { f ->
            PreviewItem(f.id, f.path, f.size, mediaId = f.id, data = f)
        }
        MediaPreviewData.items.find { it.id == m.id }?.let {
            it.initAsync(m)
            itemState.intrinsicSize = it.intrinsicSize.toSize()
        }
    }

    suspend fun setDataAsync(
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
