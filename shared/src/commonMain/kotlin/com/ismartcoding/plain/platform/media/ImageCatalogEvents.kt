package com.ismartcoding.plain.platform.media

import com.ismartcoding.plain.rust.sendEventToRust

import com.ismartcoding.plain.app.events.EventType
import com.ismartcoding.plain.app.events.HostEvent
import com.ismartcoding.plain.lib.coIO

internal fun reportImageCatalogChanged() {
    coIO { sendEventToRust(HostEvent(EventType.IMAGE_CATALOG_CHANGED, "null")) }
}
