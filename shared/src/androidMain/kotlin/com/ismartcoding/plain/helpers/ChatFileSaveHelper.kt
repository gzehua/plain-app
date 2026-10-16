package com.ismartcoding.plain.helpers

import com.ismartcoding.plain.platform.appCacheDir

import android.content.Context
import android.net.Uri
import com.ismartcoding.plain.lib.extensions.queryOpenableFileName
import java.io.File

object ChatFileSaveHelper {
    /**
     * Import a file from a content-resolver [uri] into the content-addressable
     * chat file store with two-step dedup.
     *
     * Returns a `fid:{sha256}` URI to embed in [com.ismartcoding.plain.chat.models.MessageFile.uri].
     */
    suspend fun importFromUri(
        context: Context,
        uri: Uri,
        mimeType: String = "",
    ): String {
        val tempFile = File(File(appCacheDir()), "chat_import_${System.currentTimeMillis()}_${Thread.currentThread().id}")
        tempFile.parentFile?.mkdirs()
        try {
            FileHelper.copyFile(context, uri, tempFile.absolutePath)
            val fileName = context.contentResolver.queryOpenableFileName(uri)
            val dFile = AppFileStore.importFile(tempFile.absolutePath, fileName, mimeType, deleteSrc = true)
            return AppFileStore.toFidUri(dFile)
        } finally {
            // Guard: if importFile did not consume (due to error path), clean up
            if (tempFile.exists()) tempFile.delete()
        }
    }
}
