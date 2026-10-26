package com.ismartcoding.plain.platform.system

internal actual fun initializePlatformApp() {
    IosPermissionEvents.register()
    IosFilePickerEvents.register()
    IosWindowFocusEvents.register()
}
