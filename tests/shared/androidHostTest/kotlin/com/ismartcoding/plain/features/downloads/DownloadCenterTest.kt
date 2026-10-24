package com.ismartcoding.plain.features.downloads

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.yield
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

class DownloadCenterTest {
    private data class Task(
        override val id: String,
        override val kind: String,
        override var status: DownloadStatus = DownloadStatus.PENDING,
        override var error: String = "",
    ) : DownloadTaskHandle {
        override fun flowSnapshot(): DownloadTaskHandle = copy()
    }

    @Test
    fun replacingOneQueuePreservesOtherQueueAndCopiesMutableProjections() {
        try {
            val chat = Task("chat-task", DOWNLOAD_KIND_CHAT)
            val share = Task("share-task", DOWNLOAD_KIND_SHARE)
            DownloadCenter.replaceExternal(DOWNLOAD_KIND_CHAT, listOf(chat))
            DownloadCenter.replaceExternal(DOWNLOAD_KIND_SHARE, listOf(share))
            chat.status = DownloadStatus.COMPLETED
            assertEquals(DownloadStatus.PENDING, DownloadCenter.progress.value[chat.id]?.status)
            DownloadCenter.replaceExternal(DOWNLOAD_KIND_CHAT, listOf(chat))
            assertEquals(DownloadStatus.COMPLETED, DownloadCenter.progress.value[chat.id]?.status)
            assertEquals(DownloadStatus.PENDING, DownloadCenter.progress.value[share.id]?.status)
            DownloadCenter.replaceExternal(DOWNLOAD_KIND_CHAT, emptyList())
            assertEquals(setOf(share.id), DownloadCenter.progress.value.keys)
            assertFailsWith<IllegalArgumentException> {
                DownloadCenter.replaceExternal(DOWNLOAD_KIND_SHARE, listOf(chat))
            }
            assertEquals(setOf(share.id), DownloadCenter.progress.value.keys)
        } finally {
            DownloadCenter.replaceExternal(DOWNLOAD_KIND_CHAT, emptyList())
            DownloadCenter.replaceExternal(DOWNLOAD_KIND_SHARE, emptyList())
        }
    }

    @Test
    fun unknownControlsDoNotDispatchRequests() {
        assertFalse(DownloadCenter.cancel("unknown-download"))
        assertFalse(DownloadCenter.remove("unknown-download"))
    }
}
