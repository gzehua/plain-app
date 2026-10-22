package com.ismartcoding.plain.ui.web.components

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

import com.ismartcoding.plain.preferences.*

import com.ismartcoding.plain.lib.coIO
import com.ismartcoding.plain.i18n.*
import com.ismartcoding.plain.ui.i18n.LocaleHelper
import com.ismartcoding.plain.platform.system.relaunchApp
import com.ismartcoding.plain.ui.components.DialogHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun persistMdnsHostname(
    scope: CoroutineScope,
    hostname: String,
) {
    scope.launch {
        SecuritySettingsState.setMdnsHostname(hostname)
    }
}

fun persistPort(
    scope: CoroutineScope,
    isHttps: Boolean,
    port: Int,
) {
    scope.launch(Dispatchers.IO) {
        if (isHttps) {
            Prefs.httpsPort.value = port
        } else {
            Prefs.httpPort.value = port
        }
    }
}
