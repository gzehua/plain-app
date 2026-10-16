package com.ismartcoding.plain.features.downloads

internal class DownloadQueueVersion {
    var runtimeId: String? = null
        private set
    var revision: Long = -1
        private set

    fun accept(runtime: String, next: Long): Boolean {
        require(runtime.isNotEmpty() && next >= 0)
        if (runtime == runtimeId && next <= revision) return false
        runtimeId = runtime
        revision = next
        return true
    }
}
