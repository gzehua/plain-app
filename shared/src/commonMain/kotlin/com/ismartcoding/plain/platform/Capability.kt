package com.ismartcoding.plain.platform

enum class Capability {
    MEDIA_TRASH,
    MEDIA_DELETE,
    MEDIA_MOVE,
    MEDIA_FAVORITE,
    APP_UPDATES,
    DONATION,

    MIRROR_AUDIO,
    DOC_PREVIEW,
    IMAGE_SEARCH,
    MEDIA_SCAN,
    SMS,
    CALLS,
    CALL_PHONE,
    CONTACTS,
    PACKAGES,
    NOTES,
    FEEDS,
    SCREEN_MIRROR,
    IMAGE_EDITOR,
    NOTIFICATIONS,
    CLIPBOARD,
    POMODORO,
    LAN_SHARE,
    DISK_MANAGER,
}

/**
 * Capabilities supported by this device right now; platform-specific
 * gating (e.g. Android SDK level) stays inside the actuals.
 */
expect fun getDeviceCapabilities(): List<Capability>
