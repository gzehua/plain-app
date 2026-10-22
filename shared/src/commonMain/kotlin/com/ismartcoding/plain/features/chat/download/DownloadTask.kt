package com.ismartcoding.plain.features.chat.download

import com.ismartcoding.plain.models.Peer
import kotlinx.coroutines.Job

import com.ismartcoding.plain.features.chat.models.MessageFile
import com.ismartcoding.plain.features.downloads.DOWNLOAD_KIND_CHAT
import com.ismartcoding.plain.features.downloads.DownloadStatus
import com.ismartcoding.plain.features.downloads.DownloadTaskHandle

data class DownloadTask(
    val messageFile: MessageFile,
    val peer: Peer,
    val messageId: String,
    override var status: DownloadStatus = DownloadStatus.PENDING,
    override var error: String = "",
    var downloadedSize: Long = 0,
    var downloadSpeed: Long = 0,
) : DownloadTaskHandle {
    override val id: String get() = messageFile.id
    override val kind: String get() = DOWNLOAD_KIND_CHAT

    fun isDownloading(): Boolean {
        return status == DownloadStatus.PENDING || status == DownloadStatus.DOWNLOADING
    }

    /** True when a progress overlay / pause-resume controls should be shown. */
    fun isActive(): Boolean {
        return setOf(DownloadStatus.PENDING, DownloadStatus.DOWNLOADING, DownloadStatus.PAUSED, DownloadStatus.FAILED).contains(status)
    }

    override fun flowSnapshot(): DownloadTaskHandle = copy()
}
