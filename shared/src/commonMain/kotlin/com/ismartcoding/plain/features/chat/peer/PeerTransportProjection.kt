package com.ismartcoding.plain.features.chat.peer

import com.ismartcoding.plain.api.graphql.models.PeerTransportState

import com.ismartcoding.plain.ui.chat.ChatViewModel
import com.ismartcoding.plain.features.chat.peer.transport.PeerTransportType

object PeerTransportProjection {
    internal fun apply(active: List<PeerTransportState>) {
        ChatViewModel.currentTransportMap.value = active.associate { row ->
            row.peerId to PeerTransportType.valueOf(row.transport)
        }
    }
}
