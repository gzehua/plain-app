package com.ismartcoding.plain.features.downloads

data class DownloadFailure(val path: String, val error: String)

const val DOWNLOAD_KIND_CHAT = "chat"
const val DOWNLOAD_KIND_SHARE = "share"

interface DownloadTaskHandle {
    val id: String
    val kind: String
    val status: DownloadStatus
    val error: String
    fun flowSnapshot(): DownloadTaskHandle
}
