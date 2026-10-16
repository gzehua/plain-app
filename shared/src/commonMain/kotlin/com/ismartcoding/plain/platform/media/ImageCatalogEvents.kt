package com.ismartcoding.plain.platform.media

import com.ismartcoding.plain.app.events.EventType
import com.ismartcoding.plain.app.events.HostEvent
import com.ismartcoding.plain.lib.coIO
import com.ismartcoding.plain.rust.PlainRust

internal fun reportImageCatalogChanged() {
    coIO { PlainRust.publish(HostEvent(EventType.IMAGE_CATALOG_CHANGED, "null")) }
}
