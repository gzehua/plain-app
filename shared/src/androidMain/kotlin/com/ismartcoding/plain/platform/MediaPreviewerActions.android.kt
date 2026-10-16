package com.ismartcoding.plain.platform

import coil3.imageLoader
import com.ismartcoding.plain.appContextValue
import com.ismartcoding.plain.features.media.ImageMediaStoreHelper
import com.ismartcoding.plain.helpers.ShareHelper
import java.io.File

internal actual fun sharePreviewFile(path: String, mimeType: String, mediaId: String) {
    val context = appContextValue ?: return
    if (mediaId.isNotEmpty()) {
        ShareHelper.shareUris(context, listOf(ImageMediaStoreHelper.getItemUri(mediaId)))
    } else {
        ShareHelper.shareFile(context, File(path), mimeType.ifEmpty { "image/*" })
    }
}

internal actual suspend fun useCachedPreviewFile(path: String, consume: suspend (String) -> Unit): Boolean {
    val context = appContextValue ?: return false
    val snapshot = context.imageLoader.diskCache?.openSnapshot(path) ?: return false
    snapshot.use { consume(it.data.toString()) }
    return true
}
