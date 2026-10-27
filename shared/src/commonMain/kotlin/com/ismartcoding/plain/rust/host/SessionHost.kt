package com.ismartcoding.plain.rust.host

import com.ismartcoding.plain.features.sessions.WebLoginClosedFacts
import com.ismartcoding.plain.features.sessions.WebLoginCompletedFacts
import com.ismartcoding.plain.features.sessions.WebLoginRequestFacts
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.platform.notifications.sendWebLoginNotification
import com.ismartcoding.plain.platform.system.WebActivity
import com.ismartcoding.plain.ui.app.MainViewModel
import kotlinx.serialization.json.JsonObject

internal object SessionHost {
    private val methods = setOf("systemWebActivity", "systemWebLoginRequested", "systemWebLoginClosed", "systemWebLoginCompleted")
    fun handles(method: String): Boolean = method in methods

    fun handle(method: String, params: JsonObject): HostResult {
        when (method) {
            "systemWebActivity" -> WebActivity.received()
            "systemWebLoginRequested" -> MainViewModel.pendingLoginRequest.value = JsonHelper.jsonDecodeFromElement<WebLoginRequestFacts>(params)
            "systemWebLoginClosed" -> {
                val closed = JsonHelper.jsonDecodeFromElement<WebLoginClosedFacts>(params)
                if (MainViewModel.pendingLoginRequest.value?.requestId == closed.requestId) MainViewModel.pendingLoginRequest.value = null
            }
            "systemWebLoginCompleted" -> {
                val facts = JsonHelper.jsonDecodeFromElement<WebLoginCompletedFacts>(params)
                val request = facts.request
                sendWebLoginNotification(request.browserName, request.browserVersion, request.osName, request.osVersion, facts.clientIp)
            }
            else -> error("Unsupported session host method")
        }
        return hostResult(true)
    }
}
