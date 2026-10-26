package com.ismartcoding.plain.ui.platform.system

import com.ismartcoding.plain.Prefs
import com.ismartcoding.plain.ui.theme.DarkTheme
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.UIKit.UIApplication
import platform.UIKit.UIUserInterfaceStyle
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowDidBecomeVisibleNotification
import platform.UIKit.UIWindowScene

@OptIn(ExperimentalForeignApi::class)
internal fun observeWindowTheme() {
    NSNotificationCenter.defaultCenter.addObserverForName(
        UIWindowDidBecomeVisibleNotification,
        `object` = null,
        queue = NSOperationQueue.mainQueue,
    ) { notification ->
        (notification?.`object` as? UIWindow)?.applyTheme()
    }
    CoroutineScope(Dispatchers.Main.immediate).launch {
        Prefs.darkTheme.collect {
            for (scene in UIApplication.sharedApplication.connectedScenes.filterIsInstance<UIWindowScene>()) {
                for (window in scene.windows.filterIsInstance<UIWindow>()) window.applyTheme()
            }
        }
    }
}

private fun UIWindow.applyTheme() {
    overrideUserInterfaceStyle = when (Prefs.darkTheme.value) {
        DarkTheme.ON -> UIUserInterfaceStyle.UIUserInterfaceStyleDark
        DarkTheme.OFF -> UIUserInterfaceStyle.UIUserInterfaceStyleLight
        DarkTheme.USE_DEVICE_THEME -> UIUserInterfaceStyle.UIUserInterfaceStyleUnspecified
    }
}
