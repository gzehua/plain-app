package com.ismartcoding.plain.ui.chat

import androidx.lifecycle.viewmodel.compose.viewModel

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
    val peerVM = viewModel { PeerViewModel() }
    val channelVM = viewModel { ChannelViewModel() }
    ChatListPage(navController, peerVM, channelVM)
}

@Composable
fun ChatPageRoute(navController: NavHostController, id: String) {
    val audioQueueVM = viewModel { AudioQueueViewModel() }
    val chatVM = ChatViewModel
    val peerVM = viewModel { PeerViewModel() }
    val channelVM = viewModel { ChannelViewModel() }
    ChatPage(navController, audioQueueVM, chatVM, peerVM, channelVM, id)
}
