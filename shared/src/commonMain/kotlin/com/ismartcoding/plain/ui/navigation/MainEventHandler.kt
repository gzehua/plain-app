package com.ismartcoding.plain.ui.navigation

import com.ismartcoding.plain.features.chat.RustChatStore

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.ismartcoding.plain.lib.Channel
import com.ismartcoding.plain.lib.coIO
import com.ismartcoding.plain.features.audio.AudioAction
import androidx.navigation.NavHostController
import androidx.navigation.NavDestination.Companion.hasRoute
import com.ismartcoding.plain.app.events.AudioActionEvent
import com.ismartcoding.plain.app.events.ConfirmDialogEvent
import com.ismartcoding.plain.app.events.HConfirmToAcceptLoginEvent
import com.ismartcoding.plain.app.events.LoadingDialogEvent
import com.ismartcoding.plain.app.events.HDownloadTaskDoneEvent
import com.ismartcoding.plain.ui.components.ToastEvent
import com.ismartcoding.plain.ui.audio.AudioQueueViewModel
import com.ismartcoding.plain.ui.chat.ChatViewModel
import com.ismartcoding.plain.ui.app.MainViewModel
import com.ismartcoding.plain.ui.chat.PeerViewModel
import com.ismartcoding.plain.ui.pomodoro.PomodoroViewModel
import com.ismartcoding.plain.ui.navigation.Routing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MainEventCollector(
    scope: CoroutineScope,
    mainVM: MainViewModel,
    chatVM: ChatViewModel,
    audioQueueVM: AudioQueueViewModel,
    pomodoroVM: PomodoroViewModel,
    peerVM: PeerViewModel,
    navController: NavHostController,
    onConfirmDialog: (ConfirmDialogEvent) -> Unit,
    onLoadingDialog: (LoadingDialogEvent) -> Unit,
    onToast: (ToastEvent) -> Unit,
    clearToast: () -> Unit,
) {
    var dismissToastJob: Job? = null
    val sharedFlow = Channel.sharedFlow

    LaunchedEffect(sharedFlow) {
        sharedFlow.collect { event ->
            when (event) {
                is ConfirmDialogEvent -> onConfirmDialog(event)
                is LoadingDialogEvent -> onLoadingDialog(event)
                is ToastEvent -> {
                    onToast(event)
                    dismissToastJob?.cancel()
                    dismissToastJob = coIO { delay(event.durationMs); clearToast() }
                }

                is AudioActionEvent -> {
                    if (event.action == AudioAction.MEDIA_ITEM_TRANSITION) {
                        scope.launch(Dispatchers.Default) { audioQueueVM.loadAsync() }
                    }
                }

                is com.ismartcoding.plain.app.events.HPomodoroChangedEvent -> pomodoroVM.applySnapshot(event.today)

                is HDownloadTaskDoneEvent -> {
                    scope.launch(Dispatchers.Default) {
                        val chat = RustChatStore.getById(event.downloadTask.messageId)
                        if (chat != null) {
                            chatVM.update(chat)
                        }
                    }
                }


                is HConfirmToAcceptLoginEvent -> {
                    mainVM.pendingLoginEvent.value = event
                    if (navController.currentBackStackEntry?.destination?.hasRoute<Routing.LoginRequest>() != true) {
                        navController.navigate(Routing.LoginRequest)
                    }
                }

                else -> {}
            }
        }
    }
}
