package com.ismartcoding.plain.data

import com.ismartcoding.plain.chat.models.PeerRecord

data class DPairingOutcome(val ticket: DPairingTicket, val peer: PeerRecord?, val error: String)
