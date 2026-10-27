package com.ismartcoding.plain.services

import com.ismartcoding.plain.appContext

import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.content.Context
import android.os.PowerManager
import com.ismartcoding.plain.Prefs
import com.ismartcoding.plain.lib.Channel
import com.ismartcoding.plain.lib.coMain
import kotlinx.coroutines.launch
import com.ismartcoding.plain.platform.system.WebActivity
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.events.WindowFocusChangedEvent
import com.ismartcoding.plain.powerManager
import com.ismartcoding.plain.platform.isTPlus
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

private const val CHECK_INTERVAL_MS = 60_000L
private const val INACTIVITY_TIMEOUT_MS = 30 * 60_000L

internal class HttpServerWakeLock(private val context: Context) {

    private val wakeLock: PowerManager.WakeLock = powerManager.newWakeLock(
        PowerManager.PARTIAL_WAKE_LOCK,
        "${appContext.packageName}:http_server",
    )

    private var lastActivityMs: Long = System.currentTimeMillis()

    private var inactivityJob: Job? = null
    private var eventJob: Job? = null

    private val powerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_POWER_CONNECTED -> {
                    inactivityJob?.cancel()
                    inactivityJob = null
                    acquireWakeLock()
                    scheduleInactivityTimer()
                }
                Intent.ACTION_POWER_DISCONNECTED -> {
                    lastActivityMs = System.currentTimeMillis()
                    scheduleInactivityTimer()
                }
            }
        }
    }

    fun start() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        if (isTPlus()) context.registerReceiver(powerReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        else context.registerReceiver(powerReceiver, filter)
        acquireWakeLock()
        eventJob = coMain {
            launch {
                Prefs.keepAwake.collect { enabled ->
                    inactivityJob?.cancel()
                    inactivityJob = null
                    if (enabled) acquireWakeLock()
                    lastActivityMs = System.currentTimeMillis()
                    scheduleInactivityTimer()
                }
            }
            launch {
                WebActivity.lastRequest.collect { time ->
                    if (time > 0) lastActivityMs = time
                }
            }
            Channel.sharedFlow.collect { event ->
                when (event) {
                    is WindowFocusChangedEvent -> if (event.hasFocus) {
                        lastActivityMs = System.currentTimeMillis()
                        acquireWakeLock()
                        scheduleInactivityTimer()
                    }
                }
            }
        }
    }

    fun stop() {
        context.unregisterReceiver(powerReceiver)
        eventJob?.cancel()
        eventJob = null
        inactivityJob?.cancel()
        inactivityJob = null
        releaseWakeLock()
    }

    private fun acquireWakeLock() {
        runCatching {
            if (!wakeLock.isHeld) { wakeLock.acquire(); LogCat.d("WakeLock acquired") }
        }.onFailure { LogCat.e("WakeLock acquire failed: ${it.message}") }
    }

    private fun releaseWakeLock() {
        inactivityJob?.cancel()
        inactivityJob = null
        if (wakeLock.isHeld) { wakeLock.release(); LogCat.d("WakeLock released") }
    }

    private fun isUsbConnected(): Boolean {
        val filter = IntentFilter("android.hardware.usb.action.USB_STATE")
        val intent = if (isTPlus()) context.registerReceiver(null, filter, Context.RECEIVER_NOT_EXPORTED)
        else context.registerReceiver(null, filter)
        return intent?.getBooleanExtra("connected", false) == true
    }

    private fun scheduleInactivityTimer() {
        if (Prefs.keepAwake.value || isUsbConnected()) {
            inactivityJob?.cancel()
            inactivityJob = null
            return
        }
        if (inactivityJob?.isActive == true) return
        inactivityJob = coMain {
            while (isActive) {
                delay(CHECK_INTERVAL_MS)
                if (System.currentTimeMillis() - lastActivityMs >= INACTIVITY_TIMEOUT_MS) {
                    LogCat.d("Inactivity timeout: releasing wake lock")
                    releaseWakeLock()
                    return@coMain
                }
            }
        }
    }
}
