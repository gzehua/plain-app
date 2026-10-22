package com.ismartcoding.plain.ui.app

import com.ismartcoding.plain.app.HttpServerManager

import com.ismartcoding.plain.models.PairingRequestInput
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.ismartcoding.plain.app.events.HConfirmToAcceptLoginEvent
import com.ismartcoding.plain.app.events.HChannelInviteReceivedEvent

/**
 * UI-only state for the main screen. HTTP server lifecycle (state, error,
 * start/stop/restore intent) lives entirely in
 * [com.ismartcoding.plain.app.HttpServerManager]; pages read its flows
 * directly and dispatch commands through it.
 */
class MainViewModel : ViewModel() {
    var currentRootTab = mutableIntStateOf(0)
    var pendingLoginEvent = mutableStateOf<HConfirmToAcceptLoginEvent?>(null)
    var pendingPairingRequest = mutableStateOf<PairingRequestInput?>(null)
    // The channel invite currently on top of the back stack (if any). Used by
    // HChannelInviteCanceledEvent handling to pop the right page. Not saved across
    // process death — a fresh invite will re-fire HChannelInviteReceivedEvent.
    var pendingChannelInvite = mutableStateOf<HChannelInviteReceivedEvent?>(null)
}
