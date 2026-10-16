package com.ismartcoding.plain.platform.system

import com.ismartcoding.plain.appContext
import com.ismartcoding.plain.mdns.MdnsHotspotWatcher
import com.ismartcoding.plain.platform.network.NetworkEvents

internal object HttpServerResources {
    private var hotspotWatcher: MdnsHotspotWatcher? = null
    suspend fun start() {
        if (hotspotWatcher == null) hotspotWatcher = MdnsHotspotWatcher(appContext) {
            NetworkEvents.changed()
        }.also { it.start() }
    }
    suspend fun stop() {
        hotspotWatcher?.stop()
        hotspotWatcher = null
    }
}
