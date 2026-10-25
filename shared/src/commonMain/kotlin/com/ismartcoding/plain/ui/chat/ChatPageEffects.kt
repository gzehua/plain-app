package com.ismartcoding.plain.ui.chat

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.focus.FocusManager
import com.ismartcoding.plain.app.events.PickFileResultEvent
import com.ismartcoding.plain.features.chat.download.DownloadQueue
import com.ismartcoding.plain.features.chat.data.ChatTargetType
import com.ismartcoding.plain.features.files.PickFileTag
import com.ismartcoding.plain.lib.Channel
import com.ismartcoding.plain.ui.state.launchSafe

@Composable
internal fun ChatPageEffects(
    chatVM: ChatViewModel, peerVM: PeerViewModel, channelVM: ChannelViewModel,
    targetType: ChatTargetType, focusManager: FocusManager, scrollState: LazyListState,
) {
    LaunchedEffect(peerVM) { peerVM.observe() }
    LaunchedEffect(channelVM, targetType) {
        if (targetType == ChatTargetType.CHANNEL) channelVM.observe()
    }
    LaunchedEffect(Unit) {
        launchSafe { DownloadQueue.refresh() }
    }
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
