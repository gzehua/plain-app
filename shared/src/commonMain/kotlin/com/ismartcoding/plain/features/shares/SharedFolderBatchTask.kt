package com.ismartcoding.plain.features.shares

import com.ismartcoding.plain.features.downloads.DOWNLOAD_KIND_SHARE
import com.ismartcoding.plain.features.downloads.DownloadStatus
import com.ismartcoding.plain.features.downloads.DownloadFailure
import com.ismartcoding.plain.features.downloads.DownloadTaskHandle

class SharedFolderBatchTask(
    override val id: String,
    val messageId: String,
    val type: ShareBatchType,
    val title: String,
    /** "" = public Downloads target; otherwise an absolute directory path. */
    val targetDir: String,
    val link: SharedLink,
    val urlToken: String,
    val entries: List<SharedFileDto>,
    val zipName: String = "",
) : DownloadTaskHandle {
    override val kind: String = DOWNLOAD_KIND_SHARE
    override var status: DownloadStatus = DownloadStatus.PENDING
    override var error: String = ""
    var downloadedSize: Long = 0
    var totalSize: Long = 0
    var downloadSpeed: Long = 0
    var totalFiles: Int = 0
    var doneFiles: Int = 0
    var failedFiles: Int = 0
    var currentFile: String = ""
    var packing: Boolean = false
    var failures = mutableListOf<DownloadFailure>()

    override fun flowSnapshot(): DownloadTaskHandle {
        val s = SharedFolderBatchTask(
            id, messageId, type, title, targetDir, link, urlToken, entries, zipName,
        )
        s.status = status
        s.error = error
        s.downloadedSize = downloadedSize
        s.totalSize = totalSize
        s.downloadSpeed = downloadSpeed
        s.totalFiles = totalFiles
        s.doneFiles = doneFiles
        s.failedFiles = failedFiles
        s.currentFile = currentFile
        s.packing = packing
        s.failures = failures.toList().toMutableList()
        return s
    }

    /** Overall fraction 0..1; 0 while the walker is still counting. */
    fun fraction(): Float = if (totalSize > 0) (downloadedSize.toFloat() / totalSize).coerceIn(0f, 1f) else 0f
}
