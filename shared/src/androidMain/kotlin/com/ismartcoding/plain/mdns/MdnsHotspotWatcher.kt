package com.ismartcoding.plain.mdns

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.ismartcoding.plain.platform.isTPlus
import com.ismartcoding.plain.lib.logcat.LogCat

internal class MdnsHotspotWatcher(
    private val context: Context,
    private val onStateChanged: () -> Unit,
) {
    // android.net.wifi.WifiManager constants (all @hide; raw values are stable since API 1)
    private val apStateChangedAction = "android.net.wifi.WIFI_AP_STATE_CHANGED"
    private val extraApState = "wifi_state"
    private val apStateEnabled = 13
    private val apStateDisabled = 11

    private var receiver: BroadcastReceiver? = null

    fun start() {
        if (receiver != null) return
        val r = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                val s = intent.getIntExtra(extraApState, -1)
                if (s == apStateEnabled || s == apStateDisabled) onStateChanged()
            }
        }
        val filter = IntentFilter(apStateChangedAction)
        runCatching {
            if (isTPlus()) {
                context.registerReceiver(r, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                @Suppress("UnspecifiedRegisterReceiverFlag")
                context.registerReceiver(r, filter)
            }
            receiver = r
            LogCat.d("MdnsHotspotWatcher started")
        }.onFailure { LogCat.e("MdnsHotspotWatcher start failed: ${it.message}") }
    }

    fun stop() {
        val r = receiver ?: return
        receiver = null
        runCatching { context.unregisterReceiver(r) }
            .onFailure { LogCat.e("MdnsHotspotWatcher stop failed: ${it.message}") }
    }
}
