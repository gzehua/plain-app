package com.ismartcoding.plain.ui.app

import com.ismartcoding.plain.models.PairingRequestInput
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.ismartcoding.plain.events.HConfirmToAcceptLoginEvent
import com.ismartcoding.plain.events.HChannelInviteReceivedEvent

class MainViewModel : ViewModel() {
    val shortcutMediaPath = mutableStateOf<String?>(null)
    var currentRootTab = mutableIntStateOf(0)
    var pendingLoginEvent = mutableStateOf<HConfirmToAcceptLoginEvent?>(null)
    var pendingPairingRequest = mutableStateOf<PairingRequestInput?>(null)
    // The channel invite currently on top of the back stack (if any). Used by
    // HChannelInviteCanceledEvent handling to pop the right page. Not saved across
    // process death — a fresh invite will re-fire HChannelInviteReceivedEvent.
    var pendingChannelInvite = mutableStateOf<HChannelInviteReceivedEvent?>(null)
}
