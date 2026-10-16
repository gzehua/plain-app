package com.ismartcoding.plain.platform.network

import com.ismartcoding.plain.features.chat.models.PeerRecord

expect fun startAwareIfNeeded(): Boolean

expect fun subscribeAwareForPeer(peer: PeerRecord)
