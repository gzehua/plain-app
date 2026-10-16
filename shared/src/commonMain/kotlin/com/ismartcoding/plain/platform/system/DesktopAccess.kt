package com.ismartcoding.plain.platform.system

import com.ismartcoding.plain.preferences.UserPrefs

fun setDesktopAccessEnabled(enabled: Boolean) {
    UserPrefs.desktopAccess.value = enabled
}