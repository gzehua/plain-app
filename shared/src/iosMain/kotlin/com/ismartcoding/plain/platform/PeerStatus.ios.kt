package com.ismartcoding.plain.platform

import com.ismartcoding.plain.chat.models.PeerRecord

actual fun startAwareIfNeeded(): Boolean = false

actual fun subscribeAwareForPeer(peer: PeerRecord) {}
