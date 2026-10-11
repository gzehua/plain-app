package com.ismartcoding.plain.platform

import com.ismartcoding.plain.chat.RustChatService
import com.ismartcoding.plain.chat.data.ChatTarget

suspend fun sendPickedAttachments(
    targets: List<ChatTarget>,
    uris: List<String>,
    text: String? = null,
    caption: String? = null,
    images: Boolean? = null,
    normalize: Boolean = false,
): Boolean {
    if (targets.isEmpty()) return false
    return PickedAttachments.withFiles(uris) { attachments ->
        if (uris.isNotEmpty() && attachments.isEmpty()) false
        else RustChatService.share(targets, attachments, text, caption, images, normalize)
    }
}
