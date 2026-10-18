package com.ismartcoding.plain.ui.navigation

import com.ismartcoding.plain.ui.media.extensions.collectAsStateValue
import com.ismartcoding.plain.preferences.*

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.ismartcoding.plain.ui.platform.system.applySystemBarAppearanceForDarkTheme
import com.ismartcoding.plain.platform.system.isDebugBuild
import com.ismartcoding.plain.ui.platform.system.keepScreenOn
import com.ismartcoding.plain.ui.theme.DarkTheme
import com.ismartcoding.plain.app.events.ConfirmDialogEvent
import com.ismartcoding.plain.app.events.LoadingDialogEvent
import com.ismartcoding.plain.app.events.ShowPermissionWizardEvent
import com.ismartcoding.plain.app.HttpServerManager
import com.ismartcoding.plain.lib.Channel
import com.ismartcoding.plain.app.TempData
import com.ismartcoding.plain.ui.base.DebugCornerBadge
import com.ismartcoding.plain.ui.components.ToastEvent
import com.ismartcoding.plain.ui.audio.AudioQueueViewModel
import com.ismartcoding.plain.ui.chat.ChannelViewModel
import com.ismartcoding.plain.ui.chat.ChatViewModel
import com.ismartcoding.plain.ui.feeds.FeedEntryPagerViewModel
import com.ismartcoding.plain.ui.app.MainViewModel
import com.ismartcoding.plain.ui.notes.NotesViewModel
import com.ismartcoding.plain.ui.chat.PeerViewModel
import com.ismartcoding.plain.ui.pomodoro.PomodoroViewModel
import com.ismartcoding.plain.ui.tags.TagsViewModel
import com.ismartcoding.plain.ui.navigation.Routing
import com.ismartcoding.plain.ui.dlna.DlnaReceiverOverlay
import com.ismartcoding.plain.ui.audio.player.AudioPlayerPage
import com.ismartcoding.plain.ui.media.ShortcutMediaPreviewer
import com.ismartcoding.plain.ui.home.ServiceOnboardingWizard
import com.ismartcoding.plain.ui.theme.backgroundNormal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun Main(
    navControllerState: MutableState<NavHostController?>,
    onLaunched: () -> Unit,
    mainVM: MainViewModel,
    audioQueueVM: AudioQueueViewModel,
    pomodoroVM: PomodoroViewModel,
    peerVM: PeerViewModel,
    channelVM: ChannelViewModel,
    feedTagsVM: TagsViewModel = viewModel(key = "feedTagsVM") { TagsViewModel() },
    feedEntryPagerVM: FeedEntryPagerViewModel = viewModel(key = "feedEntryPagerVM") { FeedEntryPagerViewModel() },
    noteTagsVM: TagsViewModel = viewModel(key = "noteTagsVM") { TagsViewModel() },
) {
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    navControllerState.value = navController
    val useDarkTheme = DarkTheme.isDarkTheme(UserPrefs.darkTheme.collectAsStateValue())

    var confirmDialogEvent by remember { mutableStateOf<ConfirmDialogEvent?>(null) }
    var loadingDialogEvent by remember { mutableStateOf<LoadingDialogEvent?>(null) }
    var toastState by remember { mutableStateOf<ToastEvent?>(null) }
    var showPermissionWizard by remember { mutableStateOf(false) }

    LaunchedEffect(loadingDialogEvent) {
        keepScreenOn(loadingDialogEvent != null)
    }

    LaunchedEffect(Channel.sharedFlow) {
        Channel.sharedFlow.collect { event ->
            if (event is ShowPermissionWizardEvent) {
                showPermissionWizard = true
            }
        }
    }

    LaunchedEffect(Unit) {
        onLaunched()
        scope.launch(Dispatchers.Default) { pomodoroVM.loadAsync() }
    }

    LaunchedEffect(Unit) {
        if (!RustSystemState.state.value.onboardingCompleted) {
            // singleTop: this effect re-runs on activity recreation while the
            // onboarding is still showing and the pref is not yet written
            navController.navigate(Routing.Onboarding) {
                launchSingleTop = true
            }
        }
    }

    MainEventCollector(
        scope, mainVM, audioQueueVM, pomodoroVM, peerVM, navController,
        onConfirmDialog = { confirmDialogEvent = it },
        onLoadingDialog = { loadingDialogEvent = if (it.show) it else null },
        onToast = { toastState = it },
        clearToast = { toastState = null },
    )

    // Single source of truth for the system bar appearance: the theme Compose
    // is actually rendering. SideEffect re-applies after EVERY recomposition,
    // so any competing writer (enableEdgeToEdge's config-change re-apply,
    // insets controller state, etc.) is corrected on the next frame instead of
    // leaving inverted status bar icons until restart.
    SideEffect {
        applySystemBarAppearanceForDarkTheme(useDarkTheme)
    }

    Box(modifier = Modifier.background(MaterialTheme.colorScheme.backgroundNormal)) {
        MainNavGraph(navController, mainVM, audioQueueVM, peerVM, channelVM, feedTagsVM, feedEntryPagerVM, noteTagsVM, pomodoroVM)

        // Global fullscreen audio player: hosted above all pages so playback
        // keeps working across tabs, with the page beneath visible during the
        // slide-up transition.
        val audioPlayerVisible by TempData.audioPlayerVisible.collectAsState()
        if (audioPlayerVisible) {
            AudioPlayerPage(audioQueueVM) { TempData.audioPlayerVisible.value = false }
        }

        // Home-screen shortcut media preview: overlay instead of a route so the
        // app UI stays composed beneath and closing the preview reveals it.
        val shortcutMediaPath by TempData.shortcutMediaPath.collectAsState()
        if (shortcutMediaPath != null) {
            ShortcutMediaPreviewer(shortcutMediaPath!!) { TempData.shortcutMediaPath.value = null }
        }

        DlnaReceiverOverlay()
        MainDialogs(loadingDialogEvent, confirmDialogEvent, { confirmDialogEvent = null }, toastState, { toastState = null })
        if (showPermissionWizard) {
            ServiceOnboardingWizard(
                // Permissions are resolved (or explicitly skipped) at the
                // final step — dispatch the start unconditionally.
                onStartService = {
                    showPermissionWizard = false
                    HttpServerManager.dispatchStart()
                },
                onClose = { showPermissionWizard = false },
            )
        }
        if (isDebugBuild()) {
            DebugCornerBadge(Modifier.align(Alignment.TopStart))
        }
    }
}
