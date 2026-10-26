package com.ismartcoding.plain.services

import com.ismartcoding.plain.appContext

import android.content.Context
import android.os.PowerManager
import com.ismartcoding.plain.Prefs
import com.ismartcoding.plain.lib.Channel
import com.ismartcoding.plain.lib.coIO
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.events.KeepAwakeChangedEvent
import com.ismartcoding.plain.events.PowerConnectedEvent
import com.ismartcoding.plain.events.PowerDisconnectedEvent
import com.ismartcoding.plain.events.HWebRequestReceivedEvent
import com.ismartcoding.plain.events.WindowFocusChangedEvent
import com.ismartcoding.plain.powerManager
import com.ismartcoding.plain.receivers.PlugInControlReceiver
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

    @Volatile private var lastActivityMs: Long = System.currentTimeMillis()
    @Volatile private var keepAwake: Boolean = true

    private var inactivityJob: Job? = null
    private var eventJob: Job? = null

    fun start() {
        acquireWakeLock()
        eventJob = coIO {
            keepAwake = Prefs.keepAwake.value
            scheduleInactivityTimer()
            Channel.sharedFlow.collect { event ->
                when (event) {
                    is HWebRequestReceivedEvent -> lastActivityMs = System.currentTimeMillis()
                    is WindowFocusChangedEvent -> if (event.hasFocus) {
                        lastActivityMs = System.currentTimeMillis()
                        acquireWakeLock()
                        scheduleInactivityTimer()
                    }
                    is PowerConnectedEvent -> {
                        inactivityJob?.cancel()
                        inactivityJob = null
                        acquireWakeLock()
                        scheduleInactivityTimer()
                    }
                    is PowerDisconnectedEvent -> {
                        lastActivityMs = System.currentTimeMillis()
                        scheduleInactivityTimer()
                    }
                    is KeepAwakeChangedEvent -> {
                        keepAwake = event.enabled
                        if (keepAwake) {
                            inactivityJob?.cancel()
                            inactivityJob = null
                        } else {
                            lastActivityMs = System.currentTimeMillis()
                            scheduleInactivityTimer()
                        }
                    }
                }
            }
        }
    }

    fun stop() {
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

    private fun scheduleInactivityTimer() {
        if (keepAwake || PlugInControlReceiver.isUSBConnected(context)) {
            inactivityJob?.cancel()
            inactivityJob = null
            return
        }
        if (inactivityJob?.isActive == true) return
        inactivityJob = coIO {
            while (isActive) {
                delay(CHECK_INTERVAL_MS)
                if (System.currentTimeMillis() - lastActivityMs >= INACTIVITY_TIMEOUT_MS) {
                    LogCat.d("Inactivity timeout: releasing wake lock")
                    releaseWakeLock()
                    return@coIO
                }
            }
        }
    }
}
