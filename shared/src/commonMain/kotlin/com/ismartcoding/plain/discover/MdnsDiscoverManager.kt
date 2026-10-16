package com.ismartcoding.plain.discover

import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.lib.coIO
import com.ismartcoding.plain.events.EventType
import com.ismartcoding.plain.events.HostEvent
import com.ismartcoding.plain.rust.PlainRust

object MdnsDiscoverManager {
    fun startReceiver() = RustMdnsRuntime.request("receiver")
    fun startPeriodicDiscovery() = RustMdnsRuntime.request("start")
    fun stopPeriodicDiscovery() = RustMdnsRuntime.request("stop")
    fun isDiscovering(): Boolean = RustMdnsRuntime.scanning
    fun updateAdvertisedService() = RustMdnsRuntime.request("update")
    fun browse() = RustMdnsRuntime.request("browse")
    fun scheduleRestart(reason: String) {
        LogCat.d("Network change ($reason): refreshing Rust mDNS interfaces")
        coIO { PlainRust.publish(HostEvent(EventType.NETWORK_INTERFACES_CHANGED, "null")) }
    }
}
