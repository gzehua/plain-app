package com.ismartcoding.plain.ui.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.ismartcoding.plain.Prefs
import com.ismartcoding.plain.ui.audio.AudioQueueViewModel
import com.ismartcoding.plain.ui.chat.ChannelViewModel
import com.ismartcoding.plain.ui.chat.PeerViewModel
import com.ismartcoding.plain.ui.media.extensions.collectAsStateValue
import com.ismartcoding.plain.ui.navigation.Main
import com.ismartcoding.plain.ui.pomodoro.PomodoroViewModel
import com.ismartcoding.plain.ui.theme.AppTheme
import com.ismartcoding.plain.ui.theme.DarkTheme

@Composable
fun AppContent(
    navControllerState: MutableState<NavHostController?> = remember { mutableStateOf(null) },
    onLaunched: () -> Unit = {},
    audioQueueVM: AudioQueueViewModel = viewModel { AudioQueueViewModel() },
    pomodoroVM: PomodoroViewModel = viewModel { PomodoroViewModel() },
    peerVM: PeerViewModel = viewModel { PeerViewModel() },
    channelVM: ChannelViewModel = viewModel { ChannelViewModel() },
    platformPages: (NavGraphBuilder.(NavHostController) -> Unit)? = null,
    batterySettings: (@Composable () -> Unit)? = null,
    overlays: @Composable () -> Unit = {},
) {
    AppTheme(
        useDarkTheme = DarkTheme.isDarkTheme(Prefs.darkTheme.collectAsStateValue()),
        amoledDarkTheme = Prefs.amoledDarkTheme.collectAsStateValue(),
    ) {
        Main(
            navControllerState, onLaunched, audioQueueVM, pomodoroVM, peerVM, channelVM,
            platformPages = platformPages,
            batterySettings = batterySettings,
        )
        overlays()
    }
}
