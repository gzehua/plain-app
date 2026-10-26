package com.ismartcoding.plain.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.navigationevent.NavigationEventHandler
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner

@Composable
fun PBackHandler(enabled: Boolean = true, onBack: () -> Unit) {
    val dispatcher = checkNotNull(LocalNavigationEventDispatcherOwner.current) {
        "PBackHandler requires a navigation event dispatcher"
    }.navigationEventDispatcher
    val currentOnBack by rememberUpdatedState(onBack)
    val handler = remember(dispatcher) {
        object : NavigationEventHandler<NavigationEventInfo>(NavigationEventInfo.None, isBackEnabled = false) {
            override fun onBackCompleted() = currentOnBack()
        }
    }

    LifecycleStartEffect(enabled, handler) {
        handler.isBackEnabled = enabled
        onStopOrDispose { handler.isBackEnabled = false }
    }

    DisposableEffect(dispatcher, handler) {
        dispatcher.addHandler(handler)
        onDispose { handler.remove() }
    }
}
