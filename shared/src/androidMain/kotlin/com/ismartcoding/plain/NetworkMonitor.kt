package com.ismartcoding.plain



import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import com.ismartcoding.plain.platform.network.NetworkEvents

object NetworkMonitor {
    fun init(context: Context) {
        val cm = context.getSystemService(
            ConnectivityManager::class.java
        )

        val callback = object : ConnectivityManager.NetworkCallback() {

            override fun onAvailable(network: Network) {
                NetworkEvents.changed()
            }

            override fun onLost(network: Network) {
                NetworkEvents.changed()
            }

            // Fires when the IP address changes on the same network (e.g. DHCP
            // renewal or reconnect without a full disconnect). onAvailable/onLost
            // do not cover this case, so the address bar would otherwise go stale.
            override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
                NetworkEvents.changed()
            }
        }

        cm.registerDefaultNetworkCallback(callback)

        NetworkEvents.changed()
    }

}
