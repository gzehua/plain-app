package com.ismartcoding.plain.platform

import com.ismartcoding.plain.appContext
import com.ismartcoding.plain.mdns.MdnsHotspotWatcher
import com.ismartcoding.plain.discover.MdnsDiscoverManager
import com.ismartcoding.plain.discover.RustMdnsRuntime

internal object HttpServerResources {
    private var hotspotWatcher: MdnsHotspotWatcher? = null
    suspend fun start() {
        if (hotspotWatcher == null) hotspotWatcher = MdnsHotspotWatcher(appContext) {
            MdnsDiscoverManager.scheduleRestart("hotspotStateChanged")
        }.also { it.start() }
        RustMdnsRuntime.control("publish")
    }
    suspend fun stop() {
        hotspotWatcher?.stop()
        hotspotWatcher = null
        RustMdnsRuntime.control("unpublish")
    }
}
