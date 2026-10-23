package com.ismartcoding.plain

import com.ismartcoding.plain.app.Constants
import com.ismartcoding.plain.app.initCommonPreferences

import com.ismartcoding.plain.preferences.*
import com.ismartcoding.plain.rust.RustRuntime

import android.app.Application
import android.media.AudioAttributes
import android.view.textclassifier.TextClassificationManager
import android.view.textclassifier.TextClassifier
import coil3.SingletonImageLoader
import com.ismartcoding.plain.platform.system.AppFeatureType
import com.ismartcoding.plain.ui.theme.DarkTheme
import com.ismartcoding.plain.platform.system.has
import com.ismartcoding.plain.app.events.AppEvents
import com.ismartcoding.plain.app.events.PowerConnectedEvent
import com.ismartcoding.plain.app.AppHelper
import com.ismartcoding.plain.lib.coIO
import com.ismartcoding.plain.platform.isQPlus
import com.ismartcoding.plain.platform.isUPlus
import com.ismartcoding.plain.lib.sendEvent
import com.ismartcoding.plain.platform.system.initDiskLogging
import com.ismartcoding.plain.preferences.setDarkMode
import com.ismartcoding.plain.receivers.PlugInControlReceiver
import com.ismartcoding.plain.platform.system.newImageLoader
import dalvik.system.ZipPathValidator
import com.ismartcoding.plain.app.HttpServerManager
import com.ismartcoding.plain.ui.settings.checkAppUpdate

object MainAppHelper {

    fun init(app: Application) {
        CrashHandler.install(app)
        SingletonImageLoader.setSafe { context -> newImageLoader(context) }
        RustRuntime.start()

        // Disk logging + HTTP request logging (debug=VERBOSE, release=WARN)
        initDiskLogging()

        AppEvents.register()
        HttpServerManager.ensureStarted()
        NetworkMonitor.init(app)
        if (isQPlus()) {
            try {
                audioManager.allowedCapturePolicy = AudioAttributes.ALLOW_CAPTURE_BY_ALL
            } catch (_: Exception) {
            }
        }

        if (isUPlus()) {
            ZipPathValidator.clearCallback()
        }

        try {
            val manager = app.getSystemService(TextClassificationManager::class.java)
            manager?.setTextClassifier(TextClassifier.NO_OP)
        } catch (_: Throwable) {
        }

        coIO {
            initCommonPreferences()
            setDarkMode(Prefs.darkTheme.value)
            if (Prefs.backgroundServiceEnabled.value && PlugInControlReceiver.isUSBConnected(app)) {
                sendEvent(PowerConnectedEvent())
            }

            if (AppFeatureType.CHECK_UPDATES.has()) {
                checkAppUpdate(showToast = false, automatic = true)
            }
        }
    }
}
