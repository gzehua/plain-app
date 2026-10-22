package com.ismartcoding.plain.ui.media.viewer

import com.ismartcoding.plain.features.media.MediaClient

import com.ismartcoding.plain.ui.extensions.getRotatedSize
import com.ismartcoding.plain.models.Video as MediaVideo
import com.ismartcoding.plain.models.Image as MediaImage
import androidx.compose.ui.unit.IntSize
import com.ismartcoding.plain.features.chat.models.MessageFile
import com.ismartcoding.plain.IData
import com.ismartcoding.plain.lib.extensions.getMimeType
import com.ismartcoding.plain.lib.extensions.isImageFast
import com.ismartcoding.plain.lib.extensions.isVideoFast
import kotlin.text.ifEmpty

data class PreviewItem(
    val id: String,
    var path: String = "",
    var size: Long = 0L,
    val mediaId: String = "",
    val data: IData? = null, // MessageFile, MediaVideo, MediaImage
) {
    var intrinsicSize: IntSize = IntSize.Zero
    var rotation: Int = -1

    fun isVideo(): Boolean {
        if (path.isVideoFast()) return true
        return when (val d = data) {
            is MessageFile -> d.fileName.isVideoFast()
            is MediaVideo -> true
            else -> false
        }
    }

    fun getMimeType(): String {
       return path.getMimeType().ifEmpty {
            if (data is MessageFile) data.fileName.getMimeType() else ""
        }
    }

    fun isImage(): Boolean {
        if (path.isImageFast()) return true
        return when (val d = data) {
            is MessageFile -> d.fileName.isImageFast()
            is MediaImage -> true
            else -> false
        }
    }

    suspend fun initAsync(m: MediaImage) {
        rotation = m.rotation
        intrinsicSize = m.getRotatedSize()
        if (intrinsicSize == IntSize.Zero) {
            initImageAsync()
        }
    }

    suspend fun initImageAsync() {
        val meta = MediaClient.imageInfo(path) ?: return
        rotation = meta.rotation
        intrinsicSize = IntSize(meta.width, meta.height)
    }

    suspend fun initVideoAsync() {
        resolveVideoIntrinsicSize()
    }

    private suspend fun resolveVideoIntrinsicSize() {
        val meta = MediaClient.videoInfo(path) ?: return
        rotation = meta.rotation
        intrinsicSize = if (rotation == 90 || rotation == 270) {
            IntSize(meta.height, meta.width)
        } else {
            IntSize(meta.width, meta.height)
        }
    }

    suspend fun initAsync(m: MediaVideo) {
        rotation = m.rotation
        intrinsicSize = m.getRotatedSize()
        if (intrinsicSize == IntSize.Zero) {
            resolveVideoIntrinsicSize()
        }
    }

    suspend fun initAsync(item: MessageFile) {
        if (item.fileName.isImageFast()) {
            val meta = MediaClient.imageInfo(path)
            rotation = meta?.rotation ?: 0
            intrinsicSize = if (item.width > 0 && item.height > 0) {
                IntSize(item.width, item.height)
            } else {
                meta?.let { IntSize(it.width, it.height) } ?: IntSize.Zero
            }
        } else {
            if (item.width > 0 && item.height > 0) {
                val meta = MediaClient.videoInfo(path) ?: return
                rotation = meta.rotation
                intrinsicSize = IntSize(item.width, item.height)
            } else {
                resolveVideoIntrinsicSize()
            }
        }
    }
}

object ItemType {
    const val UNKNOWN = -1
    const val IMAGE = 2
    const val VIDEO = 3
}
