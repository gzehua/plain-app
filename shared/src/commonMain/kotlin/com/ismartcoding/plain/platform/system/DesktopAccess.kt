package com.ismartcoding.plain.platform.system

import com.ismartcoding.plain.preferences.Prefs

fun setDesktopAccessEnabled(enabled: Boolean) {
    Prefs.desktopAccess.value = enabled
}