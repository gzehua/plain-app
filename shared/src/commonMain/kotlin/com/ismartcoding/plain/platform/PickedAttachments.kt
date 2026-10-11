package com.ismartcoding.plain.platform

import androidx.compose.ui.unit.IntSize
import com.ismartcoding.plain.chat.models.MessageFile
import com.ismartcoding.plain.helpers.AppFileStore
import com.ismartcoding.plain.helpers.StringHelper
import com.ismartcoding.plain.lib.extensions.isImageFast
import com.ismartcoding.plain.lib.extensions.isVideoFast
import com.ismartcoding.plain.lib.withIO
import com.ismartcoding.plain.ui.helpers.DialogHelper
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

internal object PickedAttachments {
    suspend fun <T> withFiles(uris: List<String>, normalize: Boolean, send: suspend (List<MessageFile>) -> T): T = withIO {
        val imports = mutableListOf<String>()
        try {
            val attachments = uris.distinct().mapNotNull { uri ->
                try {
                    val info = queryPickedFileInfo(uri) ?: error("Unable to read selected file information")
                    val file = withContext(NonCancellable) {
                        val relative = ".rust-import/${StringHelper.shortUUID()}"
                        val temporary = "${appDir()}/$relative"
                        try {
                            checkNotNull(copyPickedFileToAppStorage(uri, relative)) { "Unable to read selected file" }
                            AppFileStore.importFile(temporary, info.displayName, info.mimeType, deleteSrc = true)
                                .also { imports.add(it.id) }
                        } finally {
                            deleteFileAt(temporary)
                        }
                    }
                    currentCoroutineContext().ensureActive()
                    val path = AppFileStore.resolveUri(AppFileStore.toFidUri(file))
                    val size = when {
                        info.mimeType.startsWith("image/") || info.displayName.isImageFast() ->
                            getImageIntrinsicSize(path, getImageRotation(path))
                        info.mimeType.startsWith("video/") || info.displayName.isVideoFast() ->
                            getVideoIntrinsicSize(path)
                        else -> IntSize.Zero
                    }
                    val extension = getExtensionFromMimeType(info.mimeType)
                    val name = if (normalize && extension.isNotEmpty()) "${info.displayName.substringBeforeLast('.', info.displayName)}.$extension" else info.displayName
                    MessageFile(uri = AppFileStore.toFidUri(file), size = file.size, fileName = name,
                        width = size.width, height = size.height, durationMs = getMediaDurationMs(path))
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    DialogHelper.showMessage(error.message ?: "Unable to read selected file")
                    null
                }
            }
            currentCoroutineContext().ensureActive()
            withContext(NonCancellable) { send(attachments) }
        } finally {
            withContext(NonCancellable) {
                var failure: Exception? = null
                imports.forEach { id ->
                    try { AppFileStore.release(id) } catch (error: Exception) { failure = error }
                }
                failure?.let { throw it }
            }
        }
    }
}
