package com.ismartcoding.plain

import coil3.SingletonImageLoader
import com.ismartcoding.plain.events.AppEvents
import com.ismartcoding.plain.features.settings.Language
import com.ismartcoding.plain.lib.coIO
import com.ismartcoding.plain.platform.system.Capability
import com.ismartcoding.plain.platform.system.getDeviceCapabilities
import com.ismartcoding.plain.platform.system.initDiskLogging
import com.ismartcoding.plain.platform.system.initializePlatformApp
import com.ismartcoding.plain.platform.system.newImageLoader
import com.ismartcoding.plain.rust.host.audio.AudioCatalogObserver
import com.ismartcoding.plain.rust.RustRuntime
import com.ismartcoding.plain.ui.settings.checkAppUpdate

fun initializeApp(audioCatalogObserver: AudioCatalogObserver? = null) {
    SingletonImageLoader.setSafe(::newImageLoader)
    RustRuntime.start(audioCatalogObserver)
    initDiskLogging()
    AppEvents.register()
    initializePlatformApp()
    Language.observeLocale()
    if (Capability.APP_UPDATES in getDeviceCapabilities()) {
        coIO { checkAppUpdate(showToast = false, automatic = true) }
    }
}
