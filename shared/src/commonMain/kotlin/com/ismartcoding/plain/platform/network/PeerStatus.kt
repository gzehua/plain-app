package com.ismartcoding.plain.platform.network

expect fun startAwareIfNeeded(): Boolean
expect fun subscribeAwareForPeer(peer: AwarePeer, config: AwareConfig)
expect fun unsubscribeAwareForPeer(id: String)
expect fun stopAware()
