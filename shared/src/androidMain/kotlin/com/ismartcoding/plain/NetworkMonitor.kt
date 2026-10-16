package com.ismartcoding.plain

import com.ismartcoding.plain.ui.app.AppUiState

import com.ismartcoding.plain.app.TempData

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import com.ismartcoding.plain.features.nearby.MdnsDiscoverManager
import com.ismartcoding.plain.platform.system.getDeviceIP4s
import com.ismartcoding.plain.services.HttpServerService

object NetworkMonitor {
    fun init(context: Context) {
        val cm = context.getSystemService(
            ConnectivityManager::class.java
        )

        val callback = object : ConnectivityManager.NetworkCallback() {

            override fun onAvailable(network: Network) {
                check(cm)
            }

            override fun onLost(network: Network) {
                check(cm)
            }

            // Fires when the IP address changes on the same network (e.g. DHCP
            // renewal or reconnect without a full disconnect). onAvailable/onLost
            // do not cover this case, so the address bar would otherwise go stale.
            override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
                check(cm)
            }
        }

        cm.registerDefaultNetworkCallback(callback)

        check(cm)
    }

    private fun check(cm: ConnectivityManager) {
        val caps = cm.getNetworkCapabilities(cm.activeNetwork)

        val connected =
            caps?.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_VALIDATED
            ) == true
        val reason = if (connected) "Connected" else "Disconnected"
        MdnsDiscoverManager.scheduleRestart(reason)

        // Keep the home page WebAddressBar in sync with the current local IP(s).
        // AppUiState.ip4s is a Compose MutableState, so writing it here triggers
        // recomposition of the address bar.
        AppUiState.ip4s.value = getDeviceIP4s().filter { it.isNotEmpty() }
    }
}