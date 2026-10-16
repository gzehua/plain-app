package com.ismartcoding.plain.platform.network

import com.ismartcoding.plain.features.chat.models.PeerRecord

actual fun startAwareIfNeeded(): Boolean = false

actual fun subscribeAwareForPeer(peer: PeerRecord) {}
