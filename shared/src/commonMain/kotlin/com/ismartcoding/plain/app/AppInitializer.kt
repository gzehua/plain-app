package com.ismartcoding.plain.app

import com.ismartcoding.plain.features.settings.Language
import com.ismartcoding.plain.rust.host.audio.AudioEngineHost

suspend fun initAppState() {
    Language.observeLocale()
    HttpServerManager.observeBackgroundService()
    AudioEngineHost.start()
}
