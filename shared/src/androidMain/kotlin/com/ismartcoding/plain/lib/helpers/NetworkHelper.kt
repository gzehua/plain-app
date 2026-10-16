package com.ismartcoding.plain.lib.helpers

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

object NetworkHelper {
    fun isNetworkConnected(context: Context): Boolean {
        val cm = context.getSystemService(ConnectivityManager::class.java)
        val network = cm.activeNetwork
        val caps = cm.getNetworkCapabilities(network)
        return caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true &&
                caps.run {
                    hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ||
                            hasTransport(NetworkCapabilities.TRANSPORT_VPN)
                }
    }

    fun isLocalNetworkAddress(hostname: String): Boolean {
        return hostname == "localhost" ||
                hostname == "127.0.0.1" ||
                hostname.startsWith("192.168.") ||
                hostname.startsWith("10.") ||
                Regex("""172\.(1[6-9]|2[0-9]|3[0-1])\.""").containsMatchIn(hostname)
    }
}
