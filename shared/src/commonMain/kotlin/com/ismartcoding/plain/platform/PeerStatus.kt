package com.ismartcoding.plain.platform

import com.ismartcoding.plain.chat.models.PeerRecord

expect fun startAwareIfNeeded(): Boolean

expect fun subscribeAwareForPeer(peer: PeerRecord)
