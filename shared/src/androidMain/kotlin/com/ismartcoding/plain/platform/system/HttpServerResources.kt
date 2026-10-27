package com.ismartcoding.plain.platform.system

import com.ismartcoding.plain.appContext
import com.ismartcoding.plain.HttpServerManager
import com.ismartcoding.plain.events.PermissionsResultEvent
import com.ismartcoding.plain.features.telephony.sms.SmsProviderObserver
import com.ismartcoding.plain.i18n.Res
import com.ismartcoding.plain.i18n.http_server_failed
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.lib.Channel
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.lib.withIO
import com.ismartcoding.plain.mdns.MdnsHotspotWatcher
import com.ismartcoding.plain.platform.network.NetworkEvents
import com.ismartcoding.plain.platform.permissions.Permission
import com.ismartcoding.plain.Prefs
import com.ismartcoding.plain.rust.graphql.AppSubscriptions
import com.ismartcoding.plain.rust.graphql.changes
import com.ismartcoding.plain.rust.RustHttpStatus
import com.ismartcoding.plain.rust.native.RustNativeBindings
import com.ismartcoding.plain.services.PNotificationListenerService
import com.ismartcoding.plain.ui.i18n.LocaleHelper
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch

internal object HttpServerResources {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var hotspotWatcher: MdnsHotspotWatcher? = null
    private var httpResourcesError = ""
    private var notificationListenerError = ""
    @Volatile
    private var httpStarted: Boolean? = null

    val shouldEnableNotificationListener: Boolean
        get() = httpStarted == true && Prefs.desktopAccess.value && Permission.NOTIFICATION_LISTENER.name in Prefs.apiPermissions.value

    fun observe() = scope.launch(start = CoroutineStart.UNDISPATCHED) {
        val httpUpdates = flow<ResourceUpdate> {
            val output = this
            AppSubscriptions.httpServerChanged
                .onSubscription { output.emit(HttpStatusUpdate(RustNativeBindings::httpStatus)) }
                .changes().collect {
                    emit(HttpStatusUpdate(RustNativeBindings::httpStatus))
                }
        }
        merge(
            httpUpdates,
            combine(
                Prefs.apiPermissions.map { Permission.NOTIFICATION_LISTENER.name in it }.distinctUntilChanged(),
                Prefs.desktopAccess,
            ) { _, _ -> NotificationSettingUpdate },
            Channel.sharedFlow.filter { it is PermissionsResultEvent && it.has(Permission.NOTIFICATION_LISTENER) }
                .map { NotificationSettingUpdate },
        ).collect { update ->
            when (update) {
                is HttpStatusUpdate -> applyHttpStatus(update.readStatus)
                NotificationSettingUpdate -> applyNotificationListener()
            }
        }
    }

    private suspend fun applyHttpStatus(readStatus: () -> String) = withIO {
        try {
            val started = when (val state = JsonHelper.jsonDecode<RustHttpStatus>(readStatus()).state) {
                "started" -> true
                "stopped", "error" -> false
                else -> error("Unknown Rust HTTP state: $state")
            }
            httpStarted = started
            if (started) start() else stop()
            httpResourcesError = ""
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            LogCat.e("Update HTTP server resources", error)
            httpResourcesError = errorMessage(error)
        }
        publishErrors()
    }

    private suspend fun applyNotificationListener() = withIO {
        if (httpStarted == null) return@withIO
        try {
            PNotificationListenerService.toggle(appContext, shouldEnableNotificationListener)
            notificationListenerError = ""
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            LogCat.e("Update notification listener", error)
            notificationListenerError = errorMessage(error)
        }
        publishErrors()
    }

    private fun publishErrors() {
        HttpServerManager.httpServerResourcesError.value =
            listOf(httpResourcesError, notificationListenerError).filter { it.isNotEmpty() }.joinToString("\n")
    }

    private suspend fun errorMessage(error: Exception): String = error.message.orEmpty().ifEmpty {
        LocaleHelper.getStringAsync(Res.string.http_server_failed)
    }

    private suspend fun start() {
        if (hotspotWatcher == null) hotspotWatcher = MdnsHotspotWatcher(appContext) {
            NetworkEvents.changed()
        }.also { it.start() }
        applyNotificationListener()
        SmsProviderObserver.start(appContext)
    }

    private suspend fun stop() {
        hotspotWatcher?.stop()
        hotspotWatcher = null
        SmsProviderObserver.stop()
        applyNotificationListener()
    }
}

private sealed interface ResourceUpdate
private class HttpStatusUpdate(val readStatus: () -> String) : ResourceUpdate
private data object NotificationSettingUpdate : ResourceUpdate
