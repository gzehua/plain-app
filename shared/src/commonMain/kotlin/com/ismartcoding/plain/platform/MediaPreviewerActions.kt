package com.ismartcoding.plain.platform

import com.ismartcoding.plain.chat.models.MessageFile
import com.ismartcoding.plain.i18n.Res
import com.ismartcoding.plain.i18n.image_save_to
import com.ismartcoding.plain.i18n.image_save_to_failed
import com.ismartcoding.plain.lib.extensions.getFilenameFromPath
import com.ismartcoding.plain.lib.extensions.isUrl
import com.ismartcoding.plain.lib.withIO
import com.ismartcoding.plain.rust.content.RemoteFiles
import com.ismartcoding.plain.ui.components.mediaviewer.PreviewItem
import com.ismartcoding.plain.ui.helpers.DialogHelper
import kotlinx.coroutines.CancellationException

val canSavePreviewMedia: Boolean get() = Capability.FILE_EXPORT in getDeviceCapabilities()

suspend fun sharePreviewMedia(m: PreviewItem) {
    if (m.mediaId.isNotEmpty() || !m.path.isUrl()) {
        sharePreviewFile(m.path, m.getMimeType(), m.mediaId)
        return
    }
    if (useCachedPreviewFile(m.path) { sharePreviewFile(it, m.getMimeType(), "") }) return
    DialogHelper.showLoading()
    try {
        sharePreviewFile(RemoteFiles.cache(m.path), m.getMimeType(), "")
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (error: Exception) {
        DialogHelper.showMessage(error.toString())
    } finally {
        DialogHelper.hideLoading()
    }
}

suspend fun savePreviewMedia(m: PreviewItem, dir: String?) {
    check(canSavePreviewMedia) { "Unsupported capability FILE_EXPORT" }
    val name = (m.data as? MessageFile)?.fileName?.takeIf { it.isNotEmpty() }
        ?: m.path.getFilenameFromPath()
    if (!m.path.isUrl()) {
        savePreviewFile(m.path, name, dir)
        return
    }
    if (useCachedPreviewFile(m.path) { savePreviewFile(it, name, dir) }) return
    DialogHelper.showLoading()
    var temporary: String? = null
    try {
        val path = RemoteFiles.cache(m.path, name)
        temporary = path
        savePreviewFile(path, name, dir)
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (error: Exception) {
        DialogHelper.showMessage(error.toString())
    } finally {
        DialogHelper.hideLoading()
        temporary?.let { withIO { deleteFileAt(it) } }
    }
}

private suspend fun savePreviewFile(path: String, name: String, directory: String?) {
    val saved = if (directory == null) saveFileToDownloads(path, name) else copyFileToDir(path, directory, name)
    if (saved.isNotEmpty()) {
        DialogHelper.showMessage(LocaleHelper.getStringFAsync(Res.string.image_save_to, saved))
    } else {
        DialogHelper.showMessage(LocaleHelper.getStringAsync(Res.string.image_save_to_failed))
    }
}

internal expect fun sharePreviewFile(path: String, mimeType: String, mediaId: String)
internal expect suspend fun useCachedPreviewFile(path: String, consume: suspend (String) -> Unit): Boolean
