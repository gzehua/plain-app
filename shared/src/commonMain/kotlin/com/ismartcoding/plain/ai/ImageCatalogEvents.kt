package com.ismartcoding.plain.ai

import com.ismartcoding.plain.events.EventType
import com.ismartcoding.plain.events.HostEvent
import com.ismartcoding.plain.lib.coIO
import com.ismartcoding.plain.rust.PlainRust

internal fun reportImageCatalogChanged() {
    coIO { PlainRust.publish(HostEvent(EventType.IMAGE_CATALOG_CHANGED, "null")) }
}
