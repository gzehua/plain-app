package com.ismartcoding.plain.features.downloads

enum class DownloadStatus {
    PENDING,
    DOWNLOADING,
    PAUSED,
    COMPLETED,
    PARTIAL,
    FAILED,
    CANCELED,
}

val TERMINAL_DOWNLOAD_STATUSES = setOf(
    DownloadStatus.COMPLETED,
    DownloadStatus.PARTIAL,
    DownloadStatus.FAILED,
    DownloadStatus.CANCELED,
)

fun DownloadStatus.isTerminalDownloadStatus(): Boolean = this in TERMINAL_DOWNLOAD_STATUSES
