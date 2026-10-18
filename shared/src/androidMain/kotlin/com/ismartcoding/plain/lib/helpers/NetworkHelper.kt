package com.ismartcoding.plain.lib.helpers

object NetworkHelper {
    fun isLocalNetworkAddress(hostname: String): Boolean {
        return hostname == "localhost" ||
                hostname == "127.0.0.1" ||
                hostname.startsWith("192.168.") ||
                hostname.startsWith("10.") ||
                Regex("""172\.(1[6-9]|2[0-9]|3[0-1])\.""").containsMatchIn(hostname)
    }
}
