package com.ismartcoding.plain.ui.chat

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.focus.FocusManager
import com.ismartcoding.plain.app.events.PickFileResultEvent
import com.ismartcoding.plain.features.files.PickFileTag
import com.ismartcoding.plain.lib.Channel

@Composable
internal fun ChatPageEffects(chatVM: ChatViewModel, peerVM: PeerViewModel, focusManager: FocusManager, scrollState: LazyListState) {
    LaunchedEffect(chatVM, peerVM, focusManager) {
        Channel.sharedFlow.collect { event ->
            if (event is PickFileResultEvent && event.tag == PickFileTag.SEND_MESSAGE) {
                handleChatFileSelection(event, chatVM, peerVM, focusManager)
            }
        }
    }
    LaunchedEffect(chatVM, scrollState) {
        chatVM.scrollToLatest.collect { previousTopId ->
            scrollToLatest(chatVM, scrollState, previousTopId)
        }
    }
}
