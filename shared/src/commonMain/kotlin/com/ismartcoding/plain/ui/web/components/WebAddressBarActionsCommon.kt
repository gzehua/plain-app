package com.ismartcoding.plain.ui.web.components

import com.ismartcoding.plain.Prefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

import com.ismartcoding.plain.preferences.*

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun persistMdnsHostname(
    scope: CoroutineScope,
    hostname: String,
) {
    scope.launch {
        Prefs.setMdnsHostname(hostname)
    }
}

fun persistPort(
    scope: CoroutineScope,
    isHttps: Boolean,
    port: Int,
) {
    scope.launch(Dispatchers.IO) {
        if (isHttps) {
            Prefs.set(PrefKeys.HTTPS_PORT, port)
        } else {
            Prefs.set(PrefKeys.HTTP_PORT, port)
        }
    }
}
