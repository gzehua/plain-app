package com.ismartcoding.plain.platform.system

import com.ismartcoding.plain.preferences.PrefKeys

import com.ismartcoding.plain.Prefs

suspend fun setDesktopAccessEnabled(enabled: Boolean) {
    Prefs.set(PrefKeys.DESKTOP_ACCESS, enabled)
}