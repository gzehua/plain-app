package com.ismartcoding.plain.ui.chat

import com.ismartcoding.plain.ui.state.rememberViewModel

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.ismartcoding.plain.ui.audio.AudioQueueViewModel
import com.ismartcoding.plain.ui.chat.ChannelViewModel
import com.ismartcoding.plain.ui.chat.ChatViewModel
import com.ismartcoding.plain.ui.chat.PeerViewModel
import com.ismartcoding.plain.ui.chat.ChatListPage
import com.ismartcoding.plain.ui.chat.ChatPage

@Composable
fun ChatListPageRoute(navController: NavHostController) {
    val peerVM = rememberViewModel(PeerViewModel::class) { PeerViewModel() }
    val channelVM = rememberViewModel(ChannelViewModel::class) { ChannelViewModel() }
    ChatListPage(navController, peerVM, channelVM)
}

@Composable
fun ChatPageRoute(navController: NavHostController, id: String) {
    val audioQueueVM = rememberViewModel(AudioQueueViewModel::class) { AudioQueueViewModel() }
    val chatVM = ChatViewModel
    val peerVM = rememberViewModel(PeerViewModel::class) { PeerViewModel() }
    val channelVM = rememberViewModel(ChannelViewModel::class) { ChannelViewModel() }
    ChatPage(navController, audioQueueVM, chatVM, peerVM, channelVM, id)
}
