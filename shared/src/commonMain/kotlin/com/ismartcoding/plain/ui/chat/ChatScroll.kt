package com.ismartcoding.plain.ui.chat

import kotlinx.coroutines.delay

internal suspend fun scrollToLatest(
    chatVM: ChatViewModel,
    scrollState: androidx.compose.foundation.lazy.LazyListState,
    previousTopMessageId: String?,
) {
    repeat(20) {
        val currentTopMessageId = chatVM.itemsFlow.value.firstOrNull()?.id
        if (currentTopMessageId != null && currentTopMessageId != previousTopMessageId) {
            scrollState.scrollToItem(0)
            return
        }
        delay(50)
    }
    scrollState.scrollToItem(0)
}
