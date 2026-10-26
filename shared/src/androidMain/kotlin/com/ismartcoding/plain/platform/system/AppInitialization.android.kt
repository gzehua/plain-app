package com.ismartcoding.plain.platform.system

import com.ismartcoding.plain.NetworkMonitor
import com.ismartcoding.plain.app.HttpServerForegroundServiceManager
import com.ismartcoding.plain.appContext
import com.ismartcoding.plain.Prefs
import com.ismartcoding.plain.preferences.setDarkMode

internal actual fun initializePlatformApp() {
    NetworkMonitor.init(appContext)
    setDarkMode(Prefs.darkTheme.value)
    HttpServerForegroundServiceManager.observe()
    HttpServerResources.observe()
}
